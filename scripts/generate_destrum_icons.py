import math
import os
from PIL import Image, ImageDraw, ImageFilter

OUTPUT_DIR = r"C:\Users\Administrator\.gemini\antigravity\scratch\Destrum-Client\src\main\resources\assets\delta\pictures"
os.makedirs(OUTPUT_DIR, exist_ok=True)

RES = 1024
FINAL_SIZE = 256

def create_canvas():
    return Image.new("RGBA", (RES, RES), (0, 0, 0, 0))

def finalize(img, name):
    down = img.resize((FINAL_SIZE, FINAL_SIZE), Image.Resampling.LANCZOS)
    path = os.path.join(OUTPUT_DIR, name)
    down.save(path, "PNG")
    print(f"Saved {path}")

# ==========================================
# 1. DESTRUM CUBE (Singleplayer)
# ==========================================
def draw_cube():
    img = create_canvas()
    cx, cy = 512, 530
    
    # Drop shadow
    shadow = Image.new("RGBA", (RES, RES), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    s_draw.ellipse([cx - 240, cy + 240, cx + 240, cy + 330], fill=(0, 0, 0, 110))
    shadow = shadow.filter(ImageFilter.GaussianBlur(32))
    img.alpha_composite(shadow)
    
    draw = ImageDraw.Draw(img)
    
    w = 260
    h_top = 150
    h_side = 270
    
    top = (cx, cy - h_top - 120)
    tr = (cx + w, cy - 120)
    br = (cx + w, cy + h_side - 120)
    bot = (cx, cy + h_side + h_top - 120)
    bl = (cx - w, cy + h_side - 120)
    tl = (cx - w, cy - 120)
    mid = (cx, cy + 10)
    
    # Left face (soft bluish-cyan translucent glass)
    draw.polygon([tl, mid, bot, bl], fill=(215, 230, 250, 195))
    
    # Right face (slightly darker bluish glass)
    draw.polygon([mid, tr, br, bot], fill=(185, 205, 235, 210))
    
    # Top face (bright luminous white-cyan)
    draw.polygon([top, tr, mid, tl], fill=(245, 252, 255, 240))
    
    # Edge highlights (crisp anti-aliased wireframe bevel)
    edge_col = (255, 255, 255, 255)
    line_w = 16
    draw.line([tl, top, tr, br, bot, bl, tl], fill=edge_col, width=line_w, joint="curve")
    draw.line([mid, top], fill=edge_col, width=line_w)
    draw.line([mid, bot], fill=edge_col, width=line_w)
    draw.line([mid, tl], fill=edge_col, width=line_w)
    draw.line([mid, tr], fill=edge_col, width=line_w)
    
    # Emerald green badge on top-right (matching video dock_zoom_precise.png!)
    badge_cx, badge_cy = tr[0] - 10, tr[1] - 30
    bw, bh = 110, 85
    b_poly = [
        (badge_cx - bw//2, badge_cy - bh//2),
        (badge_cx + bw//2, badge_cy - bh//2),
        (badge_cx + bw//2 + 20, badge_cy + bh//2),
        (badge_cx - bw//2 + 20, badge_cy + bh//2)
    ]
    draw.polygon(b_poly, fill=(46, 204, 113, 245))
    draw.line(b_poly + [b_poly[0]], fill=(255, 255, 255, 220), width=8, joint="curve")
    # Tiny white dot / glyph in badge
    draw.ellipse([badge_cx + 5, badge_cy - 12, badge_cx + 25, badge_cy + 8], fill=(255, 255, 255, 240))
    
    finalize(img, "destrum_cube.png")

# ==========================================
# 2. DESTRUM GLOBE (Multiplayer)
# ==========================================
def draw_globe():
    img = create_canvas()
    cx, cy = 512, 512
    r = 250
    
    # Drop shadow
    shadow = Image.new("RGBA", (RES, RES), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    s_draw.ellipse([cx - 240, cy + 220, cx + 240, cy + 320], fill=(0, 0, 0, 110))
    shadow = shadow.filter(ImageFilter.GaussianBlur(32))
    img.alpha_composite(shadow)
    
    draw = ImageDraw.Draw(img)
    
    # Globe sphere (luminous deep cyan-blue gradient)
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(145, 185, 238, 205))
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=(255, 255, 255, 255), width=16)
    
    # Landmasses
    draw.ellipse([cx - 160, cy - 140, cx - 20, cy + 10], fill=(235, 245, 255, 230))
    draw.ellipse([cx + 30, cy - 120, cx + 180, cy + 40], fill=(235, 245, 255, 230))
    draw.ellipse([cx - 80, cy + 50, cx + 100, cy + 180], fill=(235, 245, 255, 230))
    
    # Tilted orbital ring
    rw, rh = 360, 105
    ring_layer = Image.new("RGBA", (RES, RES), (0, 0, 0, 0))
    rl_draw = ImageDraw.Draw(ring_layer)
    rl_draw.ellipse([cx - rw, cy - rh, cx + rw, cy + rh], outline=(255, 255, 255, 245), width=20)
    
    # Orbiting satellite node dot
    rl_draw.ellipse([cx + rw - 35, cy - 35, cx + rw + 35, cy + 35], fill=(255, 255, 255, 255))
    rl_draw.ellipse([cx + rw - 20, cy - 20, cx + rw + 20, cy + 20], fill=(255, 215, 140, 255))
    
    ring_rot = ring_layer.rotate(24, center=(cx, cy), resample=Image.Resampling.BICUBIC)
    img.alpha_composite(ring_rot)
    
    finalize(img, "destrum_globe.png")

# ==========================================
# 3. DESTRUM USER (Account Manager)
# ==========================================
def draw_user():
    img = create_canvas()
    cx, cy = 512, 512
    
    # Drop shadow
    shadow = Image.new("RGBA", (RES, RES), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    s_draw.ellipse([cx - 240, cy + 250, cx + 240, cy + 335], fill=(0, 0, 0, 110))
    shadow = shadow.filter(ImageFilter.GaussianBlur(32))
    img.alpha_composite(shadow)
    
    draw = ImageDraw.Draw(img)
    
    # Head circle
    head_r = 135
    head_cy = cy - 135
    draw.ellipse([cx - head_r, head_cy - head_r, cx + head_r, head_cy + head_r], fill=(250, 252, 255, 250), outline=(255, 255, 255, 255), width=10)
    
    # Torso dome
    body_top_y = cy + 45
    body_bot_y = cy + 295
    body_w = 265
    
    draw.chord([cx - body_w, body_top_y - 120, cx + body_w, body_bot_y + 120], start=180, end=360, fill=(250, 252, 255, 250))
    draw.arc([cx - body_w, body_top_y - 120, cx + body_w, body_bot_y + 120], start=180, end=360, fill=(255, 255, 255, 255), width=10)
    draw.rounded_rectangle([cx - body_w, body_bot_y - 60, cx + body_w, body_bot_y], radius=30, fill=(250, 252, 255, 250))
    
    finalize(img, "destrum_user.png")

# ==========================================
# 4. DESTRUM GEAR (Settings)
# ==========================================
def draw_gear():
    img = create_canvas()
    cx, cy = 512, 512
    
    # Drop shadow
    shadow = Image.new("RGBA", (RES, RES), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    s_draw.ellipse([cx - 240, cy + 230, cx + 240, cy + 330], fill=(0, 0, 0, 110))
    shadow = shadow.filter(ImageFilter.GaussianBlur(32))
    img.alpha_composite(shadow)
    
    draw = ImageDraw.Draw(img)
    
    hub_r = 210
    teeth_count = 6
    outer_r = 260
    tooth_w = 115
    
    # 6 Cog teeth (shorter, wider, professional gear shape)
    for i in range(teeth_count // 2):
        angle = i * (math.pi / (teeth_count / 2))
        dx = math.cos(angle) * outer_r
        dy = math.sin(angle) * outer_r
        draw.line([(cx - dx, cy - dy), (cx + dx, cy + dy)], fill=(250, 252, 255, 250), width=tooth_w)
        draw.ellipse([cx - dx - tooth_w//2, cy - dy - tooth_w//2, cx - dx + tooth_w//2, cy - dy + tooth_w//2], fill=(250, 252, 255, 250))
        draw.ellipse([cx + dx - tooth_w//2, cy + dy - tooth_w//2, cx + dx + tooth_w//2, cy + dy + tooth_w//2], fill=(250, 252, 255, 250))
    
    # Central hub disc
    draw.ellipse([cx - hub_r, cy - hub_r, cx + hub_r, cy + hub_r], fill=(250, 252, 255, 250))
    draw.ellipse([cx - hub_r, cy - hub_r, cx + hub_r, cy + hub_r], outline=(255, 255, 255, 255), width=12)
    
    # Center cutout axle hole
    hole_r = 95
    hole_img = Image.new("L", (RES, RES), 255)
    h_draw = ImageDraw.Draw(hole_img)
    h_draw.ellipse([cx - hole_r, cy - hole_r, cx + hole_r, cy + hole_r], fill=0)
    
    r, g, b, a = img.split()
    new_a = Image.composite(a, Image.new("L", (RES, RES), 0), hole_img)
    img.putalpha(new_a)
    
    finalize(img, "destrum_gear.png")

# ==========================================
# 5. DESTRUM CROSS (Exit 'X')
# ==========================================
def draw_cross():
    img = create_canvas()
    cx, cy = 512, 512
    
    # Drop shadow
    shadow = Image.new("RGBA", (RES, RES), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    span = 200
    thick = 95
    s_offset = 35
    s_draw.line([(cx - span + s_offset, cy - span + s_offset), (cx + span + s_offset, cy + span + s_offset)], fill=(0, 0, 0, 110), width=thick + 20)
    s_draw.line([(cx + span + s_offset, cy - span + s_offset), (cx - span + s_offset, cy + span + s_offset)], fill=(0, 0, 0, 110), width=thick + 20)
    s_draw.ellipse([cx - span + s_offset - thick//2, cy - span + s_offset - thick//2, cx - span + s_offset + thick//2, cy - span + s_offset + thick//2], fill=(0, 0, 0, 110))
    s_draw.ellipse([cx + span + s_offset - thick//2, cy + span + s_offset - thick//2, cx + span + s_offset + thick//2, cy + span + s_offset + thick//2], fill=(0, 0, 0, 110))
    s_draw.ellipse([cx + span + s_offset - thick//2, cy - span + s_offset - thick//2, cx + span + s_offset + thick//2, cy - span + s_offset + thick//2], fill=(0, 0, 0, 110))
    s_draw.ellipse([cx - span + s_offset - thick//2, cy + span + s_offset - thick//2, cx - span + s_offset + thick//2, cy + span + s_offset + thick//2], fill=(0, 0, 0, 110))
    shadow = shadow.filter(ImageFilter.GaussianBlur(30))
    img.alpha_composite(shadow)
    
    draw = ImageDraw.Draw(img)
    
    # Main 'X' cross bars
    fill_col = (252, 254, 255, 250)
    edge_col = (255, 255, 255, 255)
    
    for (p1, p2) in [
        ((cx - span, cy - span), (cx + span, cy + span)),
        ((cx + span, cy - span), (cx - span, cy + span))
    ]:
        draw.line([p1, p2], fill=fill_col, width=thick)
        # Rounded capsule endcaps
        draw.ellipse([p1[0] - thick//2, p1[1] - thick//2, p1[0] + thick//2, p1[1] + thick//2], fill=fill_col)
        draw.ellipse([p2[0] - thick//2, p2[1] - thick//2, p2[0] + thick//2, p2[1] + thick//2], fill=fill_col)
    
    # Specular light highlight along top edges
    spec_w = 16
    for (p1, p2) in [
        ((cx - span + 10, cy - span), (cx + span - 10, cy + span - 20)),
        ((cx + span - 10, cy - span), (cx - span + 10, cy + span - 20))
    ]:
        draw.line([p1, p2], fill=(255, 255, 255, 200), width=spec_w)
    
    finalize(img, "destrum_cross.png")

# ==========================================
# 6. DESTRUM PLUS ('+')
# ==========================================
def draw_plus():
    img = create_canvas()
    cx, cy = 512, 512
    
    # Drop shadow
    shadow = Image.new("RGBA", (RES, RES), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    arm = 210
    thick = 90
    s_offset = 30
    s_draw.line([(cx - arm + s_offset, cy + s_offset), (cx + arm + s_offset, cy + s_offset)], fill=(0, 0, 0, 110), width=thick + 20)
    s_draw.line([(cx + s_offset, cy - arm + s_offset), (cx + s_offset, cy + arm + s_offset)], fill=(0, 0, 0, 110), width=thick + 20)
    shadow = shadow.filter(ImageFilter.GaussianBlur(30))
    img.alpha_composite(shadow)
    
    draw = ImageDraw.Draw(img)
    
    fill_col = (252, 254, 255, 250)
    
    draw.line([(cx - arm, cy), (cx + arm, cy)], fill=fill_col, width=thick)
    draw.line([(cx, cy - arm), (cx, cy + arm)], fill=fill_col, width=thick)
    
    draw.ellipse([cx - arm - thick//2, cy - thick//2, cx - arm + thick//2, cy + thick//2], fill=fill_col)
    draw.ellipse([cx + arm - thick//2, cy - thick//2, cx + arm + thick//2, cy + thick//2], fill=fill_col)
    draw.ellipse([cx - thick//2, cy - arm - thick//2, cx + thick//2, cy - arm + thick//2], fill=fill_col)
    draw.ellipse([cx - thick//2, cy + arm - thick//2, cx + thick//2, cy + arm + thick//2], fill=fill_col)
    
    finalize(img, "destrum_plus.png")

if __name__ == "__main__":
    draw_cube()
    draw_globe()
    draw_user()
    draw_gear()
    draw_cross()
    draw_plus()
    print("All Destrum icons generated successfully!")
