# Roomba theme renders

Blender (as the `bpy` Python module, Cycles on CPU) builds the 3D art for the Roomba branch:

| Script | Output | Notes |
| --- | --- | --- |
| `apartment.py` | `assets/room-apartment.jpg` (1280x720) and `assets/2x/room-apartment.jpg` (2560x1440) | Top-down orthographic view of the room in `majorProject/src/main/assets/rooms/apartment.json`. Low sun through the window wall, with the ceiling hidden from the camera, so the light falls in hard shafts. |
| `sprites.py` | `assets/roomba.png` and `assets/dock.png`, plus 2x copies | Transparent sprites with baked shadows. The Roomba sheet has 64 headings in an 8x8 grid of 96-unit cells. The dock sheet has 4 frames of 128 units: left, top, right, bottom wall. |

The game reads the same layout JSON for collision, dust masking and dock outlets
(`RoomLayout.kt`). If you move furniture, edit the JSON and re-render.

```sh
python3.11 -m venv bpyenv && bpyenv/bin/pip install bpy==4.2.0 pillow
cd tools/render
../../bpyenv/bin/python sprites.py /tmp/out            # --preview for a quick look
../../bpyenv/bin/python apartment.py ../../majorProject/src/main/assets/rooms/apartment.json /tmp/out/apartment.jpg
```

Then copy the renders into `majorProject/src/main/assets/2x/`, and copy half-size versions
into `majorProject/src/main/assets/`.
