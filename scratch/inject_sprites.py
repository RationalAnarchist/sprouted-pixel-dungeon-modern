import os
from PIL import Image

def hex_to_rgba(hex_str):
    if hex_str is None or hex_str == '.':
        return (0, 0, 0, 0)
    hex_str = hex_str.lstrip('#')
    if len(hex_str) == 6:
        r, g, b = tuple(int(hex_str[i:i+2], 16) for i in (0, 2, 4))
        return (r, g, b, 255)
    elif len(hex_str) == 8:
        r, g, b, a = tuple(int(hex_str[i:i+2], 16) for i in (0, 2, 4, 6))
        return (r, g, b, a)
    raise ValueError(f"Invalid hex: {hex_str}")

def parse_sprite(pattern, palette):
    lines = [line.strip() for line in pattern.strip().split('\n')]
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, line in enumerate(lines):
        tokens = line.split()
        for x, tok in enumerate(tokens):
            if x < 16 and y < 16:
                color = palette.get(tok, (0, 0, 0, 0))
                img.putpixel((x, y), color)
    return img

# Palette definitions
OUTLINE = hex_to_rgba('#1a1a1a')
STEEL_DARK = hex_to_rgba('#37474f')
STEEL_MID = hex_to_rgba('#607d8b')
STEEL_LIGHT = hex_to_rgba('#b0bec5')
STEEL_WHITE = hex_to_rgba('#eceff1')
WOOD_DARK = hex_to_rgba('#3e2723')
WOOD_MID = hex_to_rgba('#6d4c41')
WOOD_LIGHT = hex_to_rgba('#a1887f')
GOLD_DARK = hex_to_rgba('#e65100')
GOLD_MID = hex_to_rgba('#f57f17')
GOLD_LIGHT = hex_to_rgba('#fbc02d')
GOLD_BRIGHT = hex_to_rgba('#fff59d')
CYAN_DARK = hex_to_rgba('#00838f')
CYAN_MID = hex_to_rgba('#00b0ff')
CYAN_LIGHT = hex_to_rgba('#80d8ff')
RED_DARK = hex_to_rgba('#b71c1c')
RED_MID = hex_to_rgba('#d50000')
RED_LIGHT = hex_to_rgba('#ff5252')
GREEN_DARK = hex_to_rgba('#1b5e20')
GREEN_MID = hex_to_rgba('#00c853')
GREEN_LIGHT = hex_to_rgba('#69f0ae')
PURPLE_DARK = hex_to_rgba('#4a148c')
PURPLE_MID = hex_to_rgba('#aa00ff')
PURPLE_LIGHT = hex_to_rgba('#e040fb')
YELLOW_MID = hex_to_rgba('#ffd600')

sprites = {}

# 1. REVOLVER
rev_palette = {
    '.': (0,0,0,0),
    'O': OUTLINE,
    's': STEEL_DARK,
    'S': STEEL_MID,
    'L': STEEL_LIGHT,
    'W': STEEL_WHITE,
    'w': WOOD_DARK,
    'Wd': WOOD_MID,
    'Wl': WOOD_LIGHT,
}
rev_grid = """
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . O O . .
. . . . . . . . O O O O S W O .
. . . . . . . O S S S S S S O .
. . . . O O O O L L L L L L O .
. . . O S S S S S S S O O O . .
. . O Wl Wd s s s O O . . . . .
. . O Wd Wd w O O . . . . . . .
. . O Wd w w O . . . . . . . . .
. . . O O O O . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
"""
sprites['revolver'] = parse_sprite(rev_grid, rev_palette)

# 2. SAPPERS_SENSOR (Radar / Trap detector)
sensor_pal = {
    '.': (0,0,0,0),
    'O': OUTLINE,
    'b': GOLD_DARK,
    'B': GOLD_MID,
    'y': GOLD_LIGHT,
    'g': GREEN_DARK,
    'G': GREEN_MID,
    'l': GREEN_LIGHT,
    'w': STEEL_WHITE
}
sensor_grid = """
. . . . . . . O . . . . . . . .
. . . . . . O l O . . . . . . .
. . . . . . O G O . . . . . . .
. . . . O O O g O O O . . . . .
. . . O B y y G y y B O . . . .
. . O b y G l w l G y b O . . .
. . O B y G w l w G y B O . . .
. . O b y G l w l G y b O . . .
. . . O B y y G y y B O . . . .
. . . . O b B B B b O . . . . .
. . . . . O O b O O . . . . . .
. . . . . . O b O . . . . . . .
. . . . . O B B B O . . . . . .
. . . . . O O O O O . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
"""
sprites['sappers_sensor'] = parse_sprite(sensor_grid, sensor_pal)

# 3. TACTICAL_MONOCLE
monocle_pal = {
    '.': (0,0,0,0),
    'O': OUTLINE,
    'b': GOLD_DARK,
    'B': GOLD_MID,
    'Y': GOLD_LIGHT,
    'c': CYAN_DARK,
    'C': CYAN_MID,
    'L': CYAN_LIGHT,
    'W': STEEL_WHITE
}
monocle_grid = """
. . . . . . . . . . . . . . . .
. . . . . O O O O . . . . . . .
. . . . O B Y Y B O . . . . . .
. . . O B c C C c B O . . . . .
. . O B c C L W C c B O . . . .
. . O Y C L W L C C Y O O . . .
. . O Y C W L L C C Y O b O . .
. . O B c C L C C c B O b O . .
. . . O B c C C c B O O b O . .
. . . . O B Y Y B O . . O O . .
. . . . . O O O O . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
"""
sprites['tactical_monocle'] = parse_sprite(monocle_grid, monocle_pal)

# 4. CLOCKWORK_DECOY
decoy_pal = {
    '.': (0,0,0,0),
    'O': OUTLINE,
    'b': GOLD_DARK,
    'B': GOLD_MID,
    's': STEEL_DARK,
    'S': STEEL_MID,
    'L': STEEL_LIGHT,
    'r': RED_MID,
    'R': RED_LIGHT
}
decoy_grid = """
. . . . . . O O . . . . . . . .
. . . . . O B B O . . . . . . .
. . . . . . O b O . . . . . . .
. . . . O O O O O O O . . . . .
. . . . O S S S S S O . . . . .
. . . . O S R S R S O . . . . .
. . . . O S s s s S O . . . . .
. . . . O O O O O O O . . . . .
. . . O S L L L L L S O . . . .
. . . O S S s s s S S O . . . .
. . . O s S s s s S s O . . . .
. . . O O S S S S S O O . . . .
. . . . O s s . s s O . . . . .
. . . . O O O . O O O . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
"""
sprites['clockwork_decoy'] = parse_sprite(decoy_grid, decoy_pal)

# 5. ADRENALINE_STIM
stim_pal = {
    '.': (0,0,0,0),
    'O': OUTLINE,
    's': STEEL_DARK,
    'S': STEEL_MID,
    'L': STEEL_LIGHT,
    'r': RED_DARK,
    'R': RED_MID,
    'w': RED_LIGHT
}
stim_grid = """
. . . . . . . . . . . . O O . .
. . . . . . . . . . . O S L O .
. . . . . . . . . . O S S O . .
. . . . . . . . O O S L O . . .
. . . . . . . O S s O O . . . .
. . . . . . O r R r O . . . . .
. . . . . O r w R r O . . . . .
. . . . O r w w R r O . . . . .
. . . O r R w R r O . . . . . .
. . O S s O O O O . . . . . . .
. O S L O . . . . . . . . . . .
. O s O . . . . . . . . . . . .
. O O . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
"""
sprites['adrenaline_stim'] = parse_sprite(stim_grid, stim_pal)

# 6. OVERCHARGE_IGNITER
igniter_pal = {
    '.': (0,0,0,0),
    'O': OUTLINE,
    's': STEEL_DARK,
    'S': STEEL_MID,
    'b': GOLD_DARK,
    'B': GOLD_MID,
    'c': CYAN_DARK,
    'C': CYAN_MID,
    'L': CYAN_LIGHT,
    'W': STEEL_WHITE
}
igniter_grid = """
. . . . . . . . . . . . . . . .
. . . . . O O O O . . . . . . .
. . . . O S S S S O . . . . . .
. . . . O b B B b O . . . . . .
. . . O S c C C c S O . . . . .
. . . O B C W L C B O . . . . .
. . . O b L W W L b O . . . . .
. . . O B C L W C B O . . . . .
. . . O S c C C c S O . . . . .
. . . . O b B B b O . . . . . .
. . . . O S S S S O . . . . . .
. . . . . O O O O . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
"""
sprites['overcharge_igniter'] = parse_sprite(igniter_grid, igniter_pal)

# 7. PURIFIERS_CORE
purifier_pal = {
    '.': (0,0,0,0),
    'O': OUTLINE,
    's': STEEL_DARK,
    'S': STEEL_LIGHT,
    'y': GOLD_MID,
    'Y': GOLD_LIGHT,
    'W': STEEL_WHITE
}
purifier_grid = """
. . . . . . . . . . . . . . . .
. . . . . . . W . . . . . . . .
. . . . . . W Y W . . . . . . .
. . . . . W Y y Y W . . . . . .
. . . . . Y y y y Y . . . . . .
. . . . O S Y y Y S O . . . . .
. . . O S S W Y W S S O . . . .
. . O S S s s s s s S S O . . .
. . O S s s s s s s s S O . . .
. . . O S s s s s s S O . . . .
. . . . O O s s s O O . . . . .
. . . . . . O s O . . . . . . .
. . . . . O S S S O . . . . . .
. . . . . O O O O O . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
"""
sprites['purifiers_core'] = parse_sprite(purifier_grid, purifier_pal)

# 8. MALICE_OVERCLOCKER
malice_pal = {
    '.': (0,0,0,0),
    'O': OUTLINE,
    's': STEEL_DARK,
    'S': STEEL_MID,
    'p': PURPLE_DARK,
    'P': PURPLE_MID,
    'l': PURPLE_LIGHT,
    'W': STEEL_WHITE
}
malice_grid = """
. . . . . . . . . . . . . . . .
. . . O . . . . . . . O . . . .
. . . O s O . . . . O s O . . .
. . . . O S O . . O S O . . . .
. . . . O s S O O S s O . . . .
. . . O S s p P p s S O . . . .
. . . O s P l W l P s O . . . .
. . . O s P W l P P s O . . . .
. . . O S s p P P p s S O . . .
. . . . O s S s S s O . . . . .
. . . . . O S s S O . . . . . .
. . . . . O s S s O . . . . . .
. . . . . O O O O O . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
"""
sprites['malice_overclocker'] = parse_sprite(malice_grid, malice_pal)

# 9. BIOMETRIC_RESONATOR
bio_pal = {
    '.': (0,0,0,0),
    'O': OUTLINE,
    's': STEEL_DARK,
    'S': STEEL_MID,
    'g': GREEN_DARK,
    'G': GREEN_MID,
    'l': GREEN_LIGHT,
    'W': STEEL_WHITE
}
bio_grid = """
. . . . . . . . . . . . . . . .
. . . . . . O O O . . . . . . .
. . . . . O S S S O . . . . . .
. . . . O S g g g S O . . . . .
. . . O S g G l G g S O . . . .
. . . O s G l W l G s O . . . .
. . . O s G W l W G s O . . . .
. . . O s G l W l G s O . . . .
. . . O S g G l G g S O . . . .
. . . . O S g g g S O . . . . .
. . . . . O S S S O . . . . . .
. . . . . . O O O . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
"""
sprites['biometric_resonator'] = parse_sprite(bio_grid, bio_pal)

# 10. MUSCLE_EXO_RIG
muscle_pal = {
    '.': (0,0,0,0),
    'O': OUTLINE,
    's': STEEL_DARK,
    'S': STEEL_MID,
    'L': STEEL_LIGHT,
    'b': GOLD_DARK,
    'B': GOLD_MID
}
muscle_grid = """
. . . . . . . . . . . . . . . .
. . . . O O O O O O . . . . . .
. . . O S L L L L S O . . . . .
. . O S L s s s s L S O . . . .
. . O L s b B B b s L O . . . .
. . O L s B B B B s L O . . . .
. . O S s b B B b s S O . . . .
. . O S L s s s s L S O . . . .
. . . O S L L L L S O . . . . .
. . . O S s B B s S O . . . . .
. . . O S L b b L S O . . . . .
. . . O S S S S S S O . . . . .
. . . . O O O O O O . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
"""
sprites['muscle_exo_rig'] = parse_sprite(muscle_grid, muscle_pal)

# 11. PHOSPHOR_FLASH_PACK
flash_pal = {
    '.': (0,0,0,0),
    'O': OUTLINE,
    's': STEEL_DARK,
    'S': STEEL_MID,
    'L': STEEL_LIGHT,
    'y': GOLD_DARK,
    'Y': YELLOW_MID,
    'w': STEEL_WHITE
}
flash_grid = """
. . . . . . O O . . . . . . . .
. . . . . O w L O . . . . . . .
. . . . . . O s O . . . . . . .
. . . . O O O O O O O . . . . .
. . . O S L S L S L S O . . . .
. . . O S Y y S Y y S O . . . .
. . . O S y Y S y Y S O . . . .
. . . O S Y y S Y y S O . . . .
. . . O S y Y S y Y S O . . . .
. . . O S L S L S L S O . . . .
. . . O s s s s s s s O . . . .
. . . . O O O O O O O . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
. . . . . . . . . . . . . . . .
"""
sprites['phosphor_flash_pack'] = parse_sprite(flash_grid, flash_pal)

# Inject into items.png
items_png_path = 'core/src/main/assets/sprites/items.png'
base_img = Image.open(items_png_path).convert('RGBA')

# 1. Revolver at row 9, col 8 (0-indexed: x=7, y=8) -> (112, 128)
base_img.paste(sprites['revolver'], (7 * 16, 8 * 16), sprites['revolver'])

# 2. Gadgets at row 13 (0-indexed: y=12) -> (0..9 * 16, 192)
gadget_keys = [
    'sappers_sensor',
    'tactical_monocle',
    'clockwork_decoy',
    'adrenaline_stim',
    'overcharge_igniter',
    'purifiers_core',
    'malice_overclocker',
    'biometric_resonator',
    'muscle_exo_rig',
    'phosphor_flash_pack'
]

for idx, key in enumerate(gadget_keys):
    x_pos = idx * 16
    y_pos = 12 * 16
    base_img.paste(sprites[key], (x_pos, y_pos), sprites[key])

base_img.save(items_png_path)
print("Successfully injected revolver and 10 gadget sprites into items.png!")
