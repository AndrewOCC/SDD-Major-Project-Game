"""Renders the Roomba rotation sheet and the charging-dock sprites (transparent, with baked shadows).

Usage:
    python tools/render/sprites.py <out_dir> [--preview]

Writes <out_dir>/roomba.png (8x8 frames, frame i faces i*360/64 degrees clockwise from +x in
screen space) and <out_dir>/dock.png (4 frames, plugged into the left, top, right, bottom wall).
Both are 2x assets: one world unit = 2 px.
"""
import math
import os
import sys

import bpy
import numpy as np

from common import configure_render, mat

ROOMBA_FRAMES = 64
ROOMBA_COLS = 8
ROOMBA_CELL_UNITS = 96
DOCK_CELL_UNITS = 128
PX_PER_UNIT = 2


def reset():
    bpy.ops.wm.read_factory_settings(use_empty=True)


def lights():
    """Same warm key as the room's window sun but steeper, so the sprite shadow stays compact."""
    sun = bpy.data.lights.new("key", "SUN")
    sun.energy = 4.2
    sun.angle = math.radians(4)
    sun.color = (1.0, 0.86, 0.70)
    key = bpy.data.objects.new("key", sun)
    key.rotation_euler = (math.radians(-38), 0, math.radians(-16))
    bpy.context.collection.objects.link(key)

    world = bpy.context.scene.world
    world.use_nodes = True
    bg = world.node_tree.nodes["Background"]
    bg.inputs["Color"].default_value = (0.62, 0.70, 0.88, 1)
    bg.inputs["Strength"].default_value = 0.55


def camera(size_m):
    cam = bpy.data.cameras.new("cam")
    cam.type = "ORTHO"
    cam.ortho_scale = size_m
    obj = bpy.data.objects.new("cam", cam)
    obj.location = (0, 0, 5)
    bpy.context.collection.objects.link(obj)
    bpy.context.scene.camera = obj


def shadow_catcher():
    bpy.ops.mesh.primitive_plane_add(size=6, location=(0, 0, 0))
    plane = bpy.context.active_object
    plane.is_shadow_catcher = True
    return plane


def cyl(r, z0, z1, material, verts=96, x=0.0, y=0.0, bev=0.0, seg=4):
    bpy.ops.mesh.primitive_cylinder_add(vertices=verts, radius=r, depth=z1 - z0, location=(x, y, (z0 + z1) / 2))
    obj = bpy.context.active_object
    obj.data.materials.append(material)
    bpy.ops.object.shade_smooth()
    if bev:
        mod = obj.modifiers.new("bevel", "BEVEL")
        mod.width = bev
        mod.segments = seg
        mod.limit_method = "ANGLE"
    return obj


def boolean(target, cutter, op="DIFFERENCE"):
    mod = target.modifiers.new("bool", "BOOLEAN")
    mod.object = cutter
    mod.operation = op
    cutter.hide_render = True
    cutter.hide_viewport = True


def build_roomba():
    """Facing +x. Game diameter 60 units = 0.60 m."""
    parts = []
    m_body = mat("body", (0.035, 0.037, 0.04), 0.35)
    m_bumper = mat("bumper", (0.55, 0.53, 0.50), 0.25, metal=0.6)
    m_lid = mat("lid", (0.045, 0.05, 0.058), 0.1)
    m_ring = mat("ring", (0.72, 0.42, 0.22), 0.28, metal=1.0)
    m_button = mat("button", (0.78, 0.78, 0.80), 0.18, metal=1.0)
    m_glass = mat("glass", (0.02, 0.06, 0.12), 0.02)

    parts.append(cyl(0.292, 0.012, 0.115, m_body, bev=0.02))

    bumper = cyl(0.300, 0.02, 0.105, m_bumper, bev=0.012)
    hole = cyl(0.286, 0.0, 0.2, m_bumper)
    boolean(bumper, hole)
    bpy.ops.mesh.primitive_cube_add(size=1, location=(-0.36, 0, 0.06))
    back = bpy.context.active_object
    back.scale = (0.72, 0.8, 0.3)
    boolean(bumper, back)
    bumper.rotation_euler.z = 0  # Front arc spans x > 0.
    parts.append(bumper)

    parts.append(cyl(0.236, 0.110, 0.135, m_lid, bev=0.012))
    ring = cyl(0.246, 0.112, 0.128, m_ring, bev=0.004)
    boolean(ring, cyl(0.236, 0.0, 0.2, m_ring))
    parts.append(ring)

    btn = cyl(0.062, 0.132, 0.145, m_button, bev=0.008)
    parts.append(btn)
    btn_ring = cyl(0.072, 0.133, 0.140, m_ring, bev=0.003)
    boolean(btn_ring, cyl(0.062, 0.0, 0.3, m_ring))
    parts.append(btn_ring)

    # Front camera window + carry-handle groove at the back.
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.05, location=(0.175, 0, 0.134))
    cam = bpy.context.active_object
    cam.scale = (0.7, 1.4, 0.25)
    cam.data.materials.append(m_glass)
    bpy.ops.object.shade_smooth()
    parts.append(cam)
    groove = cyl(0.17, 0.133, 0.138, mat("groove", (0.01, 0.01, 0.012), 0.5), verts=96, x=-0.0)
    boolean(groove, cyl(0.155, 0.0, 0.3, m_lid))
    bpy.ops.mesh.primitive_cube_add(size=1, location=(0.1, 0, 0.13))
    keep = bpy.context.active_object
    keep.scale = (0.5, 0.6, 0.1)
    boolean(groove, keep)
    parts.append(groove)

    pivot = bpy.data.objects.new("roomba", None)
    bpy.context.collection.objects.link(pivot)
    for p in parts:
        p.parent = pivot
    return pivot


def build_dock():
    """Plugged into the top wall: the wall face is at y = +0.52 (screen up), pad centred at the origin."""
    m_shell = mat("dock_shell", (0.86, 0.84, 0.80), 0.3)
    m_teak = mat("dock_teak", (0.38, 0.16, 0.05), 0.35)
    m_pad = mat("dock_pad", (0.03, 0.032, 0.035), 0.55)
    m_brass = mat("dock_brass", (0.8, 0.6, 0.3), 0.25, metal=1.0)
    parts = []

    pad = cyl(0.50, 0.0, 0.012, m_pad, bev=0.006)
    parts.append(pad)
    # Ramp lip at the front of the pad.
    lip = cyl(0.50, 0.0, 0.016, m_shell, bev=0.005)
    boolean(lip, cyl(0.47, -0.1, 0.1, m_shell))
    parts.append(lip)

    # Tower against the wall: white shell with a teak cap, the plug cable running into the wall.
    bpy.ops.mesh.primitive_cube_add(size=1, location=(0, 0.40, 0.17))
    tower = bpy.context.active_object
    tower.scale = (0.62, 0.24, 0.34)
    tower.data.materials.append(m_shell)
    bev = tower.modifiers.new("bevel", "BEVEL")
    bev.width = 0.05
    bev.segments = 6
    parts.append(tower)
    bpy.ops.mesh.primitive_cube_add(size=1, location=(0, 0.40, 0.345))
    cap = bpy.context.active_object
    cap.scale = (0.56, 0.19, 0.03)
    cap.data.materials.append(m_teak)
    bev = cap.modifiers.new("bevel", "BEVEL")
    bev.width = 0.012
    bev.segments = 3
    parts.append(cap)

    for side in (-1, 1):
        bpy.ops.mesh.primitive_cube_add(size=1, location=(side * 0.16, 0.0, 0.016))
        contact = bpy.context.active_object
        contact.scale = (0.08, 0.28, 0.008)
        contact.data.materials.append(m_brass)
        parts.append(contact)

    pivot = bpy.data.objects.new("dock", None)
    bpy.context.collection.objects.link(pivot)
    for p in parts:
        p.parent = pivot
    return pivot


def render_to_array(path):
    bpy.context.scene.render.filepath = path
    bpy.ops.render.render(write_still=True)
    img = bpy.data.images.load(path)
    w, h = img.size
    arr = np.array(img.pixels[:], dtype=np.float32).reshape(h, w, 4)[::-1]
    bpy.data.images.remove(img)
    return arr


def save_sheet(frames, cols, path):
    h, w, _ = frames[0].shape
    rows = (len(frames) + cols - 1) // cols
    sheet = np.zeros((rows * h, cols * w, 4), dtype=np.float32)
    for i, f in enumerate(frames):
        r, c = divmod(i, cols)
        sheet[r * h:(r + 1) * h, c * w:(c + 1) * w] = f
    img = bpy.data.images.new("sheet", cols * w, rows * h, alpha=True)
    img.pixels = sheet[::-1].ravel()
    img.filepath_raw = path
    img.file_format = "PNG"
    img.save()


def main():
    args = sys.argv[1:]
    out_dir = args[0]
    preview = "--preview" in args
    tmp = os.path.join(out_dir, "_frames")
    os.makedirs(tmp, exist_ok=True)
    samples = 16 if preview else 128

    # Roomba sheet.
    reset()
    cell = ROOMBA_CELL_UNITS * PX_PER_UNIT
    configure_render(cell, cell, samples, transparent=True)
    bpy.context.scene.view_settings.look = "AgX - Medium High Contrast"
    lights()
    camera(ROOMBA_CELL_UNITS / 100.0)
    shadow_catcher()
    roomba = build_roomba()
    frames = []
    count = 8 if preview else ROOMBA_FRAMES
    for i in range(count):
        facing = i * 360.0 / count
        roomba.rotation_euler.z = -math.radians(facing)  # Clockwise on screen.
        frames.append(render_to_array(os.path.join(tmp, f"roomba_{i:02d}.png")))
    save_sheet(frames, ROOMBA_COLS, os.path.join(out_dir, "roomba.png"))

    # Dock: frames for walls 0 left, 1 top, 2 right, 3 bottom. The modelled dock is on the top wall.
    reset()
    cell = DOCK_CELL_UNITS * PX_PER_UNIT
    configure_render(cell, cell, samples, transparent=True)
    bpy.context.scene.view_settings.look = "AgX - Medium High Contrast"
    lights()
    camera(DOCK_CELL_UNITS / 100.0)
    shadow_catcher()
    dock = build_dock()
    frames = []
    for wall, rot in ((0, 90), (1, 0), (2, -90), (3, 180)):
        dock.rotation_euler.z = math.radians(rot)
        frames.append(render_to_array(os.path.join(tmp, f"dock_{wall}.png")))
    save_sheet(frames, 4, os.path.join(out_dir, "dock.png"))


if __name__ == "__main__":
    main()
