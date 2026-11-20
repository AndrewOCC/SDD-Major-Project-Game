"""Shared Blender helpers for the Roomba-theme asset renders.

Coordinates: x/y are game world units (1 unit = 1 cm, y grows down), z is metres up.
"""
import math

import bpy

WORLD_W = 1280
WORLD_H = 720


def px(x):
    return x / 100.0


def py(y):
    return (WORLD_H - y) / 100.0


def reset_scene():
    bpy.ops.wm.read_factory_settings(use_empty=True)


def configure_render(width, height, samples, transparent=False):
    scene = bpy.context.scene
    scene.render.engine = "CYCLES"
    scene.cycles.device = "CPU"
    scene.cycles.samples = samples
    scene.cycles.use_denoising = True
    scene.render.resolution_x = width
    scene.render.resolution_y = height
    scene.render.resolution_percentage = 100
    scene.render.film_transparent = transparent
    scene.render.image_settings.file_format = "PNG"
    scene.render.image_settings.color_mode = "RGBA" if transparent else "RGB"
    scene.view_settings.view_transform = "AgX"
    scene.view_settings.look = "AgX - Punchy"
    if scene.world is None:
        scene.world = bpy.data.worlds.new("world")


def setup_camera(cx, cy, width_units, height=10.0, x=None):
    cam = bpy.data.cameras.new("cam")
    cam.type = "ORTHO"
    cam.ortho_scale = width_units / 100.0
    cam.clip_end = 100
    obj = bpy.data.objects.new("cam", cam)
    obj.location = (px(cx), py(cy), height)
    bpy.context.collection.objects.link(obj)
    bpy.context.scene.camera = obj
    return obj


def setup_sun(elevation_deg, azimuth_deg, strength, angle_deg=0.6, color=(1.0, 0.82, 0.62)):
    """Sun coming from the top of the screen (world -y, Blender +y) rotated by azimuth."""
    sun = bpy.data.lights.new("sun", "SUN")
    sun.energy = strength
    sun.angle = math.radians(angle_deg)
    sun.color = color
    obj = bpy.data.objects.new("sun", sun)
    # Sun points along -Z by default. Tilt toward -y (light travels down-screen).
    obj.rotation_euler = (math.radians(-(90 - elevation_deg)), 0, math.radians(azimuth_deg))
    bpy.context.collection.objects.link(obj)

    world = bpy.context.scene.world
    world.use_nodes = True
    bg = world.node_tree.nodes["Background"]
    bg.inputs["Color"].default_value = (0.55, 0.65, 0.85, 1)
    bg.inputs["Strength"].default_value = 0.12
    return obj


_materials = {}


def mat(name, color, roughness, metal=0.0, sheen=0.0):
    if name in _materials and _materials[name].name in bpy.data.materials:
        return _materials[name]
    m = bpy.data.materials.new(name)
    m.use_nodes = True
    bsdf = m.node_tree.nodes["Principled BSDF"]
    bsdf.inputs["Base Color"].default_value = (*color, 1)
    bsdf.inputs["Roughness"].default_value = roughness
    bsdf.inputs["Metallic"].default_value = metal
    if sheen:
        bsdf.inputs["Sheen Weight"].default_value = sheen
    _materials[name] = m
    return m


def assign(obj, material):
    obj.data.materials.clear()
    obj.data.materials.append(material)


def bevel(obj, width, segments=3):
    mod = obj.modifiers.new("bevel", "BEVEL")
    mod.width = width
    mod.segments = segments
    mod.limit_method = "ANGLE"
    return mod


def _link(obj):
    bpy.context.collection.objects.link(obj)
    return obj


def add_box(x, y, w, h, z0, z1, material, bev=0.0):
    """Axis-aligned box covering world rect (x, y, w, h) from z0 to z1 metres."""
    bpy.ops.mesh.primitive_cube_add(size=1)
    obj = bpy.context.active_object
    obj.scale = (w / 100.0, h / 100.0, max(z1 - z0, 0.001))
    obj.location = (px(x + w / 2), py(y + h / 2), (z0 + z1) / 2)
    bpy.ops.object.transform_apply(location=False, rotation=False, scale=True)
    assign(obj, material)
    if bev:
        bevel(obj, bev)
    return obj


def add_cylinder(cx, cy, r, z0, z1, material, verts=32, bev=0.0):
    bpy.ops.mesh.primitive_cylinder_add(vertices=verts, radius=r / 100.0, depth=max(z1 - z0, 0.001),
                                        location=(px(cx), py(cy), (z0 + z1) / 2))
    obj = bpy.context.active_object
    assign(obj, material)
    bpy.ops.object.shade_smooth()
    if bev:
        bevel(obj, bev)
    return obj


def add_sphere(cx, cy, r, z, material, scale_z=1.0):
    bpy.ops.mesh.primitive_uv_sphere_add(segments=32, ring_count=16, radius=r / 100.0, location=(px(cx), py(cy), z))
    obj = bpy.context.active_object
    obj.scale.z = scale_z
    assign(obj, material)
    bpy.ops.object.shade_smooth()
    return obj


def hidden_from_camera(obj):
    obj.visible_camera = False
    obj.visible_glossy = False
