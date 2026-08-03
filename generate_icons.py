import math
from PIL import Image, ImageDraw

def create_fine_lines_icon(size=512):
    # First Cyber Radar icon with FINE THIN CRISP LINES & compact square dots
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    
    bg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    bg_draw = ImageDraw.Draw(bg)
    r = 90
    
    # Outer dark cyan glow
    for i in range(25, 0, -1):
        alpha = int(40 * (1 - i / 25))
        bg_draw.rounded_rectangle([20 - i, 20 - i, size - 20 + i, size - 20 + i], radius=r+i, fill=(0, 180, 216, alpha))
        
    # Main dark navy background
    bg_draw.rounded_rectangle([30, 30, size - 30, size - 30], radius=r, fill=(13, 17, 23, 255), outline=(42, 57, 74, 255), width=4)
    img = Image.alpha_composite(img, bg)
    
    draw = ImageDraw.Draw(img)
    cx, cy = size // 2, size // 2
    radius = 160
    
    # Hexagon angles
    angles = [(2 * math.pi * i / 6.0) - (math.pi / 2.0) for i in range(6)]
    
    # Very Fine Thin Web Rings (1px - 2px)
    levels = [0.25, 0.50, 0.75, 1.0]
    for lev in levels:
        ring_pts = []
        for a in angles:
            rx = cx + math.cos(a) * (radius * lev)
            ry = cy + math.sin(a) * (radius * lev)
            ring_pts.append((rx, ry))
        color = (61, 82, 106, 255) if lev == 1.0 else (33, 46, 61, 180)
        width = 2 if lev == 1.0 else 1 # Very thin web lines
        for i in range(6):
            draw.line([ring_pts[i], ring_pts[(i+1)%6]], fill=color, width=width)
            
    # Very Fine Axis rays (1px)
    for a in angles:
        rx = cx + math.cos(a) * radius
        ry = cy + math.sin(a) * radius
        draw.line([(cx, cy), (rx, ry)], fill=(42, 57, 74, 180), width=1) # Very thin axis rays
        
    # Filled Radar Polygon
    poly_ratios = [0.95, 0.65, 0.40, 0.50, 0.75, 0.85]
    poly_pts = []
    for i, a in enumerate(angles):
        px = cx + math.cos(a) * (radius * poly_ratios[i])
        py = cy + math.sin(a) * (radius * poly_ratios[i])
        poly_pts.append((px, py))
        
    fill_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    fill_draw = ImageDraw.Draw(fill_layer)
    fill_draw.polygon(poly_pts, fill=(0, 180, 216, 70))
    img = Image.alpha_composite(img, fill_layer)
    
    # Polygon Outline (Thin 3px stroke)
    draw = ImageDraw.Draw(img)
    for i in range(6):
        draw.line([poly_pts[i], poly_pts[(i+1)%6]], fill=(0, 212, 255, 255), width=3)
        
    # 6 COMPACT SQUARE VERTEX DOTS (Compact 8x8px)
    colors = [
        (0, 180, 216, 255),   # Travel (Cyan)
        (239, 68, 68, 255),   # Combat (Red)
        (234, 179, 8, 255),   # Trading (Yellow)
        (34, 197, 94, 255),   # Agriculture (Green)
        (168, 85, 247, 255),  # Building (Purple)
        (249, 115, 22, 255)   # Mining (Orange)
    ]
    for i, (px, py) in enumerate(poly_pts):
        s = 4 # Compact 8x8px square dot
        draw.rectangle([px - s, py - s, px + s, py + s], fill=colors[i], outline=(255, 255, 255, 255), width=1)
        
    return img

if __name__ == "__main__":
    icon = create_fine_lines_icon()
    icon.save("assets/screenshots/icon_fine_lines.png")
    
    icon_small = icon.resize((128, 128), Image.Resampling.LANCZOS)
    icon_small.save("src/main/resources/assets/freshstats/icon.png")
    print("Fine thin lines icon generated successfully!")
