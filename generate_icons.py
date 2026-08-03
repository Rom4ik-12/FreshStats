import math
from PIL import Image, ImageDraw

def create_icon_1(size=512):
    # Option 1: Clean Cyber Neon Hexagonal Radar Icon (Dotless)
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    
    bg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    bg_draw = ImageDraw.Draw(bg)
    r = 90
    
    for i in range(25, 0, -1):
        alpha = int(40 * (1 - i / 25))
        bg_draw.rounded_rectangle([20 - i, 20 - i, size - 20 + i, size - 20 + i], radius=r+i, fill=(0, 180, 216, alpha))
        
    bg_draw.rounded_rectangle([30, 30, size - 30, size - 30], radius=r, fill=(13, 17, 23, 255), outline=(42, 57, 74, 255), width=6)
    img = Image.alpha_composite(img, bg)
    
    draw = ImageDraw.Draw(img)
    cx, cy = size // 2, size // 2
    radius = 160
    
    angles = [(2 * math.pi * i / 6.0) - (math.pi / 2.0) for i in range(6)]
    
    # Draw web rings
    levels = [0.25, 0.50, 0.75, 1.0]
    for lev in levels:
        ring_pts = []
        for a in angles:
            rx = cx + math.cos(a) * (radius * lev)
            ry = cy + math.sin(a) * (radius * lev)
            ring_pts.append((rx, ry))
        color = (61, 82, 106, 255) if lev == 1.0 else (33, 46, 61, 200)
        width = 4 if lev == 1.0 else 2
        for i in range(6):
            draw.line([ring_pts[i], ring_pts[(i+1)%6]], fill=color, width=width)
            
    # Draw axis rays
    for a in angles:
        rx = cx + math.cos(a) * radius
        ry = cy + math.sin(a) * radius
        draw.line([(cx, cy), (rx, ry)], fill=(42, 57, 74, 255), width=3)
        
    # Filled Radar Polygon
    poly_ratios = [0.95, 0.65, 0.40, 0.50, 0.75, 0.85]
    poly_pts = []
    for i, a in enumerate(angles):
        px = cx + math.cos(a) * (radius * poly_ratios[i])
        py = cy + math.sin(a) * (radius * poly_ratios[i])
        poly_pts.append((px, py))
        
    fill_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    fill_draw = ImageDraw.Draw(fill_layer)
    fill_draw.polygon(poly_pts, fill=(0, 180, 216, 85))
    img = Image.alpha_composite(img, fill_layer)
    
    # Polygon Outline (Clean stroke without dots)
    draw = ImageDraw.Draw(img)
    for i in range(6):
        draw.line([poly_pts[i], poly_pts[(i+1)%6]], fill=(0, 212, 255, 255), width=6)
        
    return img

def create_icon_2(size=512):
    # Option 2: Pixel-Art Minecraft Radar Badge (Dotless)
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    r = 80
    draw.rounded_rectangle([30, 30, size - 30, size - 30], radius=r, fill=(20, 24, 34, 255), outline=(245, 158, 11, 255), width=10)
    draw.rounded_rectangle([44, 44, size - 44, size - 44], radius=r-14, fill=(15, 23, 42, 255), outline=(30, 41, 59, 255), width=4)
    
    cx, cy = size // 2, size // 2
    radius = 150
    angles = [(2 * math.pi * i / 6.0) - (math.pi / 2.0) for i in range(6)]
    
    for lev in [0.3, 0.6, 0.9]:
        ring_pts = [(cx + math.cos(a) * (radius * lev), cy + math.sin(a) * (radius * lev)) for a in angles]
        for i in range(6):
            draw.line([ring_pts[i], ring_pts[(i+1)%6]], fill=(51, 65, 85, 255), width=3)
            
    for a in angles:
        draw.line([(cx, cy), (cx + math.cos(a) * radius, cy + math.sin(a) * radius)], fill=(51, 65, 85, 255), width=3)
        
    ratios = [0.85, 0.95, 0.60, 0.75, 0.90, 0.65]
    poly_pts = [(cx + math.cos(a) * (radius * ratios[i]), cy + math.sin(a) * (radius * ratios[i])) for i, a in enumerate(angles)]
    
    fill_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    fill_draw = ImageDraw.Draw(fill_layer)
    fill_draw.polygon(poly_pts, fill=(245, 158, 11, 100))
    img = Image.alpha_composite(img, fill_layer)
    
    draw = ImageDraw.Draw(img)
    for i in range(6):
        draw.line([poly_pts[i], poly_pts[(i+1)%6]], fill=(251, 191, 36, 255), width=7)
        
    return img

def create_icon_3(size=512):
    # Option 3: Minimalist Modern Emblem (Dotless Glass)
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    r = 100
    draw.rounded_rectangle([30, 30, size - 30, size - 30], radius=r, fill=(17, 24, 39, 255), outline=(56, 189, 248, 255), width=8)
    
    cx, cy = size // 2, size // 2
    radius = 165
    angles = [(2 * math.pi * i / 6.0) - (math.pi / 2.0) for i in range(6)]
    
    draw.ellipse([cx-radius, cy-radius, cx+radius, cy+radius], fill=None, outline=(31, 41, 55, 255), width=4)
    draw.ellipse([cx-radius*0.6, cy-radius*0.6, cx+radius*0.6, cy+radius*0.6], fill=None, outline=(31, 41, 55, 255), width=3)
    
    for a in angles:
        draw.line([(cx, cy), (cx + math.cos(a) * radius, cy + math.sin(a) * radius)], fill=(55, 65, 81, 255), width=3)
        
    r1 = [0.90, 0.70, 0.85, 0.60, 0.75, 0.80]
    r2 = [0.65, 0.95, 0.50, 0.85, 0.55, 0.90]
    
    p1 = [(cx + math.cos(a) * (radius * r1[i]), cy + math.sin(a) * (radius * r1[i])) for i, a in enumerate(angles)]
    p2 = [(cx + math.cos(a) * (radius * r2[i]), cy + math.sin(a) * (radius * r2[i])) for i, a in enumerate(angles)]
    
    f1 = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    fd1 = ImageDraw.Draw(f1)
    fd1.polygon(p1, fill=(14, 165, 233, 90))
    img = Image.alpha_composite(img, f1)
    
    f2 = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    fd2 = ImageDraw.Draw(f2)
    fd2.polygon(p2, fill=(168, 85, 247, 90))
    img = Image.alpha_composite(img, f2)
    
    draw = ImageDraw.Draw(img)
    for i in range(6):
        draw.line([p1[i], p1[(i+1)%6]], fill=(56, 189, 248, 255), width=5)
        draw.line([p2[i], p2[(i+1)%6]], fill=(192, 132, 252, 255), width=5)
        
    return img

if __name__ == "__main__":
    icon1 = create_icon_1()
    icon1.save("assets/screenshots/icon_option_1.png")
    
    icon2 = create_icon_2()
    icon2.save("assets/screenshots/icon_option_2.png")
    
    icon3 = create_icon_3()
    icon3.save("assets/screenshots/icon_option_3.png")
    
    icon1_small = icon1.resize((128, 128), Image.Resampling.LANCZOS)
    icon1_small.save("src/main/resources/assets/freshstats/icon.png")
    print("Icons re-generated successfully!")
