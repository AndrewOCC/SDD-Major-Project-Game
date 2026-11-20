"""Renders the mid-century / brutalist apartment backdrop for the Roomba theme.

Usage (Blender 4.2 as a Python module, e.g. `pip install bpy==4.2.0`):
    python tools/render/apartment.py <layout.json> <out.png> [--preview]

The room is built from tools-side primitives so it can be re-rendered whenever the layout
changes. World units match the game: 1280x720, 1 unit = 1 cm, y grows down.
"""
import json
import math
import sys

import bpy

from common import (WORLD_H, WORLD_W, add_box, add_cylinder, add_sphere, assign, bevel, configure_render,
                    hidden_from_camera, mat, px, py, reset_scene, setup_camera, setup_sun)

WALL_H = 2.7


def concrete():
    m = bpy.data.materials.new("concrete")
    m.use_nodes = True
    nt = m.node_tree
    bsdf = nt.nodes["Principled BSDF"]
    bsdf.inputs["Roughness"].default_value = 0.92
    coord = nt.nodes.new("ShaderNodeTexCoord")
    noise = nt.nodes.new("ShaderNodeTexNoise")
    noise.inputs["Scale"].default_value = 4.0
    noise.inputs["Detail"].default_value = 8.0
    nt.links.new(coord.outputs["Object"], noise.inputs["Vector"])
    boards = nt.nodes.new("ShaderNodeTexWave")
    boards.inputs["Scale"].default_value = 3.2
    boards.inputs["Distortion"].default_value = 1.2
    nt.links.new(coord.outputs["Object"], boards.inputs["Vector"])
    mix = nt.nodes.new("ShaderNodeMix")
    mix.data_type = "RGBA"
    mix.inputs["Factor"].default_value = 0.25
    nt.links.new(noise.outputs["Fac"], mix.inputs[6])
    nt.links.new(boards.outputs["Fac"], mix.inputs[7])
    ramp = nt.nodes.new("ShaderNodeValToRGB")
    ramp.color_ramp.elements[0].color = (0.50, 0.49, 0.47, 1)
    ramp.color_ramp.elements[1].color = (0.64, 0.63, 0.60, 1)
    nt.links.new(mix.outputs[2], ramp.inputs["Fac"])
    nt.links.new(ramp.outputs["Color"], bsdf.inputs["Base Color"])
    bump = nt.nodes.new("ShaderNodeBump")
    bump.inputs["Strength"].default_value = 0.12
    nt.links.new(noise.outputs["Fac"], bump.inputs["Height"])
    nt.links.new(bump.outputs["Normal"], bsdf.inputs["Normal"])
    return m


def teak_planks():
    m = bpy.data.materials.new("teak_floor")
    m.use_nodes = True
    nt = m.node_tree
    bsdf = nt.nodes["Principled BSDF"]
    bsdf.inputs["Roughness"].default_value = 0.42
    coord = nt.nodes.new("ShaderNodeTexCoord")
    brick = nt.nodes.new("ShaderNodeTexBrick")
    brick.inputs["Scale"].default_value = 1.0
    brick.inputs["Mortar Size"].default_value = 0.004
    brick.inputs["Brick Width"].default_value = 1.6
    brick.inputs["Row Height"].default_value = 0.16
    brick.offset = 0.37
    brick.inputs["Color1"].default_value = (0.20, 0.08, 0.025, 1)
    brick.inputs["Color2"].default_value = (0.27, 0.11, 0.035, 1)
    brick.inputs["Mortar"].default_value = (0.12, 0.06, 0.03, 1)
    nt.links.new(coord.outputs["Object"], brick.inputs["Vector"])
    grain = nt.nodes.new("ShaderNodeTexWave")
    grain.inputs["Scale"].default_value = 18.0
    grain.inputs["Distortion"].default_value = 6.0
    grain.inputs["Detail"].default_value = 4.0
    grain.bands_direction = "Y"
    nt.links.new(coord.outputs["Object"], grain.inputs["Vector"])
    mix = nt.nodes.new("ShaderNodeMix")
    mix.data_type = "RGBA"
    mix.blend_type = "MULTIPLY"
    mix.inputs["Factor"].default_value = 0.22
    nt.links.new(brick.outputs["Color"], mix.inputs[6])
    nt.links.new(grain.outputs["Color"], mix.inputs[7])
    nt.links.new(mix.outputs[2], bsdf.inputs["Base Color"])
    bump = nt.nodes.new("ShaderNodeBump")
    bump.inputs["Strength"].default_value = 0.2
    nt.links.new(brick.outputs["Fac"], bump.inputs["Height"])
    nt.links.new(bump.outputs["Normal"], bsdf.inputs["Normal"])
    return m


def rug_material():
    m = bpy.data.materials.new("rug")
    m.use_nodes = True
    nt = m.node_tree
    bsdf = nt.nodes["Principled BSDF"]
    bsdf.inputs["Roughness"].default_value = 1.0
    coord = nt.nodes.new("ShaderNodeTexCoord")
    mapping = nt.nodes.new("ShaderNodeMapping")
    mapping.inputs["Rotation"].default_value = (0, 0, math.radians(45))
    mapping.inputs["Scale"].default_value = (1.6, 1.6, 1.6)
    nt.links.new(coord.outputs["Object"], mapping.inputs["Vector"])
    checker = nt.nodes.new("ShaderNodeTexChecker")
    checker.inputs["Scale"].default_value = 1.0
    checker.inputs["Color1"].default_value = (0.05, 0.16, 0.17, 1)
    checker.inputs["Color2"].default_value = (0.70, 0.60, 0.42, 1)
    nt.links.new(mapping.outputs["Vector"], checker.inputs["Vector"])
    nt.links.new(checker.outputs["Color"], bsdf.inputs["Base Color"])
    noise = nt.nodes.new("ShaderNodeTexNoise")
    noise.inputs["Scale"].default_value = 120.0
    bump = nt.nodes.new("ShaderNodeBump")
    bump.inputs["Strength"].default_value = 0.5
    nt.links.new(noise.outputs["Fac"], bump.inputs["Height"])
    nt.links.new(bump.outputs["Normal"], bsdf.inputs["Normal"])
    return m


def build_room(layout):
    wall = layout["wall"]
    m_concrete = concrete()

    floor = add_box(0, 0, WORLD_W, WORLD_H, -0.02, 0.0, teak_planks())
    floor.name = "floor"

    # Perimeter walls; the top wall is mostly floor-to-ceiling glazing split by concrete fins.
    door = layout.get("door")
    if door and door["wall"] == 3:
        add_box(0, WORLD_H - wall, door["x0"], wall, 0, WALL_H, m_concrete, bev=0.01)
        add_box(door["x1"], WORLD_H - wall, WORLD_W - door["x1"], wall, 0, WALL_H, m_concrete, bev=0.01)
        # Lintel above the opening keeps the wall top continuous from above.
        add_box(door["x0"], WORLD_H - wall, door["x1"] - door["x0"], wall, 2.1, WALL_H, m_concrete)
    else:
        add_box(0, WORLD_H - wall, WORLD_W, wall, 0, WALL_H, m_concrete, bev=0.01)
    add_box(0, 0, wall, WORLD_H, 0, WALL_H, m_concrete, bev=0.01)
    add_box(WORLD_W - wall, 0, wall, WORLD_H, 0, WALL_H, m_concrete, bev=0.01)
    for x0, x1 in layout["windows"]:
        add_box(0, 0, x0, wall, 0, WALL_H, m_concrete, bev=0.01)
        add_box(x1, 0, WORLD_W - x1, wall, 0, WALL_H, m_concrete, bev=0.01)
        spacing = layout["mullionSpacing"]
        x = x0 + spacing
        while x < x1 - 20:
            add_box(x - 6, 0, 12, wall + 6, 0, WALL_H, m_concrete, bev=0.005)
            x += spacing
        # Steel window frame sill line (low, so the sun still pours in).
        add_box(x0, wall - 4, x1 - x0, 4, 0, 0.04, mat("steel", (0.05, 0.05, 0.05), 0.35, metal=1.0))

    # Ceiling slab over the interior only: the sun reaches the floor solely through the glazing,
    # while the wall tops stay sunlit. Invisible to the camera.
    ceiling = add_box(wall - 1, wall - 1, WORLD_W - 2 * wall + 2, WORLD_H - 2 * wall + 2,
                      WALL_H - 0.03, WALL_H - 0.01, m_concrete)
    hidden_from_camera(ceiling)

    # Pleated curtains gathered at the ends of the glazing.
    m_curtain = mat("curtain", (0.62, 0.20, 0.04), 0.85, sheen=0.6)
    for x0, x1 in layout["curtains"]:
        x = x0
        i = 0
        while x < x1:
            add_cylinder(x, wall + 12 + (i % 2) * 6, 10, 0.02, WALL_H - 0.05, m_curtain, verts=12)
            x += 13
            i += 1

    rug = layout["rug"]
    add_box(rug["x"], rug["y"], rug["w"], rug["h"], 0.0, 0.012, mat("rug_border", (0.62, 0.22, 0.06), 1.0))
    add_box(rug["x"] + 22, rug["y"] + 22, rug["w"] - 44, rug["h"] - 44, 0.0, 0.016, rug_material())
    add_box(rug["x"] + 14, rug["y"] + 14, rug["w"] - 28, rug["h"] - 28, 0.0, 0.014,
            mat("rug_line", (0.78, 0.56, 0.10), 1.0))


# --- Furniture --------------------------------------------------------------------------

TEAK = (0.42, 0.18, 0.06)
WALNUT = (0.20, 0.09, 0.04)
MUSTARD = (0.78, 0.48, 0.05)


def legs(x, y, w, h, top, m, inset=8, r=2.5):
    for lx, ly in ((x + inset, y + inset), (x + w - inset, y + inset), (x + inset, y + h - inset), (x + w - inset, y + h - inset)):
        add_cylinder(lx, ly, r, 0, top, m, verts=10)


def sofa(f):
    x, y, w, h = f["x"], f["y"], f["w"], f["h"]
    m_teak = mat("teak", TEAK, 0.35)
    m_fabric = mat("mustard", MUSTARD, 0.9, sheen=0.5)
    legs(x, y, w, h, 0.14, m_teak, inset=10)
    add_box(x, y, w, h, 0.14, 0.24, m_teak, bev=0.01)
    arm = 16
    add_box(x, y, w, arm, 0.24, 0.56, m_teak, bev=0.01)
    add_box(x, y + h - arm, w, arm, 0.24, 0.56, m_teak, bev=0.01)
    back = 24
    add_box(x + 2, y + arm, back, h - 2 * arm, 0.24, 0.80, m_fabric, bev=0.05)
    seats = 3
    seat_h = (h - 2 * arm) / seats
    for i in range(seats):
        add_box(x + back + 2, y + arm + i * seat_h + 2, w - back - 6, seat_h - 4, 0.24, 0.44, m_fabric, bev=0.045)
    pillow = add_box(x + back + 4, y + arm + 10, 26, 34, 0.42, 0.56, mat("pillow", (0.55, 0.16, 0.05), 0.9), bev=0.04)
    pillow.rotation_euler.z = math.radians(20)


def side_table(f):
    cx, cy, r = f["x"] + f["w"] / 2, f["y"] + f["h"] / 2, f["w"] / 2
    m_teak = mat("teak", TEAK, 0.35)
    for a in range(3):
        ang = a * 2 * math.pi / 3
        add_cylinder(cx + math.cos(ang) * r * 0.55, cy + math.sin(ang) * r * 0.55, 2.2, 0, 0.5, m_teak, verts=8)
    add_cylinder(cx, cy, r, 0.5, 0.53, m_teak, verts=48, bev=0.006)
    add_cylinder(cx + 6, cy - 4, 9, 0.53, 0.75, mat("ceramic", (0.86, 0.83, 0.76), 0.3), verts=24)


def arc_lamp(f):
    bx, by = f["x"] + f["w"] / 2, f["y"] + f["h"] / 2
    sx, sy = f["reachX"], f["reachY"]
    add_box(bx - 22, by - 14, 44, 28, 0, 0.18, mat("marble", (0.85, 0.84, 0.80), 0.2), bev=0.01)
    curve = bpy.data.curves.new("arc", "CURVE")
    curve.dimensions = "3D"
    curve.bevel_depth = 0.012
    spline = curve.splines.new("BEZIER")
    spline.bezier_points.add(1)
    p0, p1 = spline.bezier_points
    p0.co = (px(bx), py(by), 0.18)
    p0.handle_left = p0.co
    p0.handle_right = (px(bx), py(by), 2.3)
    p1.co = (px(sx), py(sy), 1.95)
    p1.handle_left = (px(sx) - 0.4 * (px(sx) - px(bx)), py(sy) - 0.4 * (py(sy) - py(by)), 2.35)
    p1.handle_right = p1.co
    obj = bpy.data.objects.new("arc", curve)
    bpy.context.collection.objects.link(obj)
    assign(obj, mat("chrome", (0.8, 0.8, 0.8), 0.15, metal=1.0))
    shade = add_sphere(sx, sy, 22, 1.8, mat("brass", (0.75, 0.55, 0.2), 0.25, metal=1.0), scale_z=0.5)
    shade.name = "shade"


def lounge(f):
    cx, cy = f["x"] + f["w"] / 2, f["y"] + f["h"] / 2
    m_walnut = mat("walnut", WALNUT, 0.3)
    m_leather = mat("leather", (0.04, 0.035, 0.03), 0.45)
    parts = [
        add_box(cx - 42, cy - 36, 84, 70, 0.30, 0.42, m_walnut, bev=0.03),
        add_box(cx - 38, cy - 32, 76, 62, 0.40, 0.50, m_leather, bev=0.04),
        add_box(cx - 42, cy - 64, 84, 30, 0.42, 0.95, m_walnut, bev=0.03),
        add_box(cx - 38, cy - 60, 76, 24, 0.50, 0.92, m_leather, bev=0.04),
        add_cylinder(cx, cy, 5, 0, 0.32, mat("chrome", (0.8, 0.8, 0.8), 0.15, metal=1.0), verts=12),
    ]
    parts[2].rotation_euler.x = math.radians(-18)
    parts[3].rotation_euler.x = math.radians(-18)
    ottoman = [
        add_box(cx - 34, cy + 46, 68, 46, 0.22, 0.32, m_walnut, bev=0.03),
        add_box(cx - 30, cy + 50, 60, 38, 0.30, 0.40, m_leather, bev=0.04),
        add_cylinder(cx, cy + 69, 4, 0, 0.24, mat("chrome", (0.8, 0.8, 0.8), 0.15, metal=1.0), verts=12),
    ]
    pivot = bpy.data.objects.new("lounge_pivot", None)
    pivot.location = (px(cx), py(cy), 0)
    bpy.context.collection.objects.link(pivot)
    bpy.context.view_layer.update()
    for p in parts + ottoman:
        p.parent = pivot
        p.matrix_parent_inverse = pivot.matrix_world.inverted()
    pivot.rotation_euler.z = math.radians(-f.get("rot", 0))


def credenza(f):
    x, y, w, h = f["x"], f["y"], f["w"], f["h"]
    m_teak = mat("teak", TEAK, 0.35)
    legs(x, y, w, h, 0.16, mat("black", (0.03, 0.03, 0.03), 0.4), inset=12)
    add_box(x, y, w, h, 0.16, 0.74, m_teak, bev=0.01)
    # Table lamp, books, a vase.
    add_cylinder(x + 60, y + h / 2, 14, 0.74, 0.98, mat("ceramic_green", (0.15, 0.32, 0.22), 0.3), verts=32)
    add_cylinder(x + 60, y + h / 2, 26, 0.98, 1.18, mat("shade", (0.92, 0.88, 0.78), 0.8), verts=48)
    colours = [(0.6, 0.12, 0.05), (0.08, 0.2, 0.3), (0.8, 0.7, 0.4), (0.1, 0.1, 0.1)]
    for i, c in enumerate(colours):
        b = add_box(x + 200 + i * 2, y + 10 + i * 3, 70 - i * 6, 34, 0.74 + i * 0.03, 0.77 + i * 0.03, mat(f"book{i}", c, 0.6))
        b.rotation_euler.z = math.radians(i * 7 - 8)
    add_cylinder(x + w - 50, y + h / 2, 10, 0.74, 1.02, mat("vase", (0.85, 0.55, 0.2), 0.25), verts=32)


def bookshelf(f):
    x, y, w, h = f["x"], f["y"], f["w"], f["h"]
    m_teak = mat("teak", TEAK, 0.35)
    add_box(x, y, w, h, 0, 1.85, m_teak, bev=0.008)
    for i in range(6):
        add_box(x + 8, y + 12 + i * 40, w - 16, 26, 1.85, 1.86 + 0.02 * (i % 3),
                mat(f"top{i}", (0.25 + 0.1 * (i % 3), 0.12, 0.05), 0.5))
    add_cylinder(x + w / 2, y + h - 30, 14, 1.85, 2.05, mat("terracotta", (0.55, 0.25, 0.12), 0.8), verts=24)


def plant(f):
    cx, cy, r = f["x"] + f["w"] / 2, f["y"] + f["h"] / 2, f["w"] / 2
    add_cylinder(cx, cy, r * 0.55, 0, 0.45, mat("terracotta", (0.55, 0.25, 0.12), 0.8), verts=32, bev=0.01)
    add_cylinder(cx, cy, r * 0.48, 0.44, 0.46, mat("soil", (0.08, 0.05, 0.03), 1.0), verts=32)
    m_leaf = mat("leaf", (0.06, 0.22, 0.08), 0.45)
    n = 11
    for i in range(n):
        ang = i * 2 * math.pi / n + (i % 2) * 0.3
        dist = r * (0.45 + 0.35 * ((i * 7) % 5) / 4)
        leaf = add_sphere(cx + math.cos(ang) * dist, cy + math.sin(ang) * dist, r * 0.42,
                          0.75 + 0.25 * (i % 3), m_leaf, scale_z=0.12)
        leaf.scale = (1.0, 0.45, 0.12)
        leaf.rotation_euler = (0, math.radians(-20), -ang)


BUILDERS = {
    "sofa": sofa, "sideTable": side_table, "arcLamp": arc_lamp, "lounge": lounge,
    "credenza": credenza, "bookshelf": bookshelf, "plant": plant,
}


# Times of day. Sun angles are degrees; "sky" is the world colour and strength.
VARIANTS = {
    "day": dict(sun=(30, -16, 11.0, (1.0, 0.82, 0.62)), sky=((0.55, 0.65, 0.85), 0.12), fill=220),
    "golden": dict(sun=(17, -34, 8.5, (1.0, 0.66, 0.36)), sky=((0.62, 0.55, 0.62), 0.09), fill=150),
    "sunset": dict(sun=(8, -48, 7.0, (1.0, 0.42, 0.18)), sky=((0.55, 0.32, 0.38), 0.06), fill=90),
    "dusk": dict(sun=None, sky=((0.16, 0.20, 0.42), 0.12), fill=70, fill_color=(0.55, 0.62, 1.0)),
    "night": dict(sun=(40, 12, 0.9, (0.55, 0.68, 1.0)), sky=((0.05, 0.08, 0.18), 0.10), fill=55,
                  fill_color=(0.45, 0.55, 1.0)),
    "door": dict(sun=(40, 12, 0.9, (0.55, 0.68, 1.0)), sky=((0.05, 0.08, 0.18), 0.10), fill=55,
                 fill_color=(0.45, 0.55, 1.0), door_open=True, porch=True, visitor=True),
    "lights": dict(sun=(40, 12, 0.9, (0.55, 0.68, 1.0)), sky=((0.05, 0.08, 0.18), 0.10), fill=40,
                   fill_color=(1.0, 0.8, 0.6), door_open=True, porch=True, lamps=True),
}


def add_light(kind, name, x, y, z, energy, color, size=0.1):
    light = bpy.data.lights.new(name, kind)
    light.energy = energy
    light.color = color
    if kind == "POINT":
        light.shadow_soft_size = size
    obj = bpy.data.objects.new(name, light)
    obj.location = (px(x), py(y), z)
    bpy.context.collection.objects.link(obj)
    return obj


def door_leaf(layout, open_door):
    door = layout.get("door")
    if not door:
        return
    wall = layout["wall"]
    width = door["x1"] - door["x0"]
    m_door = mat("door", (0.30, 0.13, 0.05), 0.4)
    leaf = add_box(door["x1"] - width, WORLD_H - wall + 4, width, 5, 0.01, 2.08, m_door, bev=0.004)
    if open_door:
        # Hinged at the right jamb, swung ~100 degrees into the room.
        pivot = bpy.data.objects.new("hinge", None)
        pivot.location = (px(door["x1"]), py(WORLD_H - wall + 6), 0)
        bpy.context.collection.objects.link(pivot)
        bpy.context.view_layer.update()
        leaf.parent = pivot
        leaf.matrix_parent_inverse = pivot.matrix_world.inverted()
        pivot.rotation_euler.z = math.radians(-100)
    # Threshold strip.
    add_box(door["x0"], WORLD_H - wall, width, wall, 0, 0.01, mat("steel", (0.05, 0.05, 0.05), 0.35, metal=1.0))


def porch(layout, visitor):
    door = layout["door"]
    cx = (door["x0"] + door["x1"]) / 2
    # Outdoor floor so the light has something to bounce off, and the porch lamp beyond the door.
    add_box(door["x0"] - 200, WORLD_H, door["x1"] - door["x0"] + 400, 300, -0.02, 0.0, mat("path", (0.3, 0.29, 0.27), 0.9))
    add_light("POINT", "porch", cx + 10, WORLD_H + 260, 2.2, 3200, (1.0, 0.72, 0.42), size=0.04)
    if visitor:
        m = mat("visitor", (0.05, 0.05, 0.05), 0.8)
        # Standing just off to the left of the doorway, keys in hand: a long shadow in the light.
        vx, vy = cx - 30, WORLD_H + 120
        body = add_cylinder(vx, vy, 20, 0.0, 1.45, m, verts=16)
        body.scale.y = 0.6
        head = add_sphere(vx, vy, 12, 1.62, m)
        bag = add_box(vx - 34, vy - 6, 14, 26, 0.6, 0.95, m)
        for o in (body, head, bag):
            hidden_from_camera(o)


def main():
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else sys.argv[1:]
    layout_path, out_path = args[0], args[1]
    preview = "--preview" in args
    variant_name = "day"
    if "--variant" in args:
        variant_name = args[args.index("--variant") + 1]
    v = VARIANTS[variant_name]
    with open(layout_path) as fh:
        layout = json.load(fh)

    reset_scene()
    configure_render(640 if preview else 2560, 360 if preview else 1440, 24 if preview else 96)
    setup_camera(WORLD_W / 2, WORLD_H / 2, WORLD_W)
    if v["sun"]:
        elev, az, strength, color = v["sun"]
        setup_sun(elevation_deg=elev, azimuth_deg=az, strength=strength, color=color)
    world = bpy.context.scene.world
    world.use_nodes = True
    bg = world.node_tree.nodes["Background"]
    bg.inputs["Color"].default_value = (*v["sky"][0], 1)
    bg.inputs["Strength"].default_value = v["sky"][1]

    build_room(layout)
    for f in layout["furniture"]:
        BUILDERS[f["type"]](f)
    door_leaf(layout, v.get("door_open", False))
    if v.get("porch"):
        porch(layout, v.get("visitor", False))
    if v.get("lamps"):
        add_light("POINT", "pendant", 640, 380, 2.4, 900, (1.0, 0.74, 0.46), size=0.25)
        add_light("POINT", "arc", 300, 270, 1.75, 160, (1.0, 0.70, 0.40), size=0.1)
        add_light("POINT", "table", 490, 663, 1.05, 70, (1.0, 0.70, 0.40), size=0.08)

    # Soft, cool fill from above so the playfield stays readable outside the sun shafts.
    fill = bpy.data.lights.new("fill", "AREA")
    fill.shape = "RECTANGLE"
    fill.size = px(WORLD_W)
    fill.size_y = px(WORLD_H)
    fill.energy = v["fill"]
    fill.color = v.get("fill_color", (0.85, 0.9, 1.0))
    fobj = bpy.data.objects.new("fill", fill)
    fobj.location = (px(WORLD_W / 2), py(WORLD_H / 2), WALL_H - 0.05)
    bpy.context.collection.objects.link(fobj)

    if out_path.lower().endswith((".jpg", ".jpeg")):
        settings = bpy.context.scene.render.image_settings
        settings.file_format = "JPEG"
        settings.color_mode = "RGB"
        settings.quality = 90
    bpy.context.scene.render.filepath = out_path
    bpy.ops.render.render(write_still=True)


if __name__ == "__main__":
    main()
