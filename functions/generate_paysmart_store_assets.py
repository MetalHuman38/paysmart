from __future__ import annotations

from pathlib import Path
from textwrap import wrap

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageFont


COMPANY_DIR = Path(r"H:\company")
OUTPUT_DIR = COMPANY_DIR / "store-assets"

FEATURE_GRAPHIC = OUTPUT_DIR / "paysmart_feature_graphic_v4_ultra.png"
LEFT_CARD_ONLY = OUTPUT_DIR / "paysmart_left_hero_card.png"
LOGO_MARK_PNG = OUTPUT_DIR / "paysmart_logo_refresh_mark.png"
LOGO_LOCKUP_PNG = OUTPUT_DIR / "paysmart_logo_refresh_lockup.png"
LOGO_PREVIEW_PNG = OUTPUT_DIR / "paysmart_logo_refresh_preview.png"
LOGO_MARK_SVG = OUTPUT_DIR / "paysmart_logo_refresh_mark.svg"

SCREENSHOT_EXCHANGE = COMPANY_DIR / "Screenshot_20260320_105637.png"
SCREENSHOT_PROFILE = COMPANY_DIR / "profile-light-theme.png"
SCREENSHOT_VERIFY = COMPANY_DIR / "verifyemail.png"
SCREENSHOT_SECURITY = COMPANY_DIR / "security.png"

CANVAS_SIZE = (1024, 500)

BG_TOP = "#091512"
BG_BOTTOM = "#133129"
SURFACE = "#12211D"
SURFACE_ALT = "#1A2E28"
TEXT_PRIMARY = "#F5FBF8"
TEXT_SECONDARY = "#C6D8D2"
ACCENT = "#2ED08A"
ACCENT_SOFT = "#7AE6C1"
ACCENT_DARK = "#0F3B31"


def main() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    mark = create_logo_mark(1024)
    save_logo_assets(mark)
    feature = create_feature_graphic(mark)
    feature.save(FEATURE_GRAPHIC, format="PNG", optimize=True)
    left_card = create_left_hero_card(mark)
    left_card.save(LEFT_CARD_ONLY, format="PNG", optimize=True)

    print("Generated:")
    print(f" - {FEATURE_GRAPHIC}")
    print(f" - {LEFT_CARD_ONLY}")
    print(f" - {LOGO_MARK_PNG}")
    print(f" - {LOGO_LOCKUP_PNG}")
    print(f" - {LOGO_PREVIEW_PNG}")
    print(f" - {LOGO_MARK_SVG}")


def create_feature_graphic(mark: Image.Image) -> Image.Image:
    canvas = Image.new("RGB", CANVAS_SIZE, BG_TOP)
    draw_linear_gradient(canvas, BG_TOP, BG_BOTTOM)

    glow(canvas, (700, 20, 1180, 500), "#19C98A", 125, 118)
    glow(canvas, (120, 40, 470, 360), "#173F34", 92, 72)
    glow(canvas, (560, 40, 900, 260), "#7AE6C1", 52, 40)
    glow(canvas, (-100, 260, 320, 620), "#102A23", 110, 88)

    draw = ImageDraw.Draw(canvas)
    title_font = load_font(68, bold=True)

    brand_mark = mark.resize((72, 72), Image.Resampling.LANCZOS)
    canvas.paste(brand_mark, (60, 78), brand_mark)
    draw.text((60, 176), "PaySmart", font=title_font, fill=TEXT_PRIMARY)
    draw.rounded_rectangle(
        (62, 268, 230, 274),
        radius=3,
        fill=ACCENT,
    )

    security_card = make_screen_card(
        SCREENSHOT_SECURITY,
        target_height=360,
        crop_top=74,
        crop_bottom=76,
    )
    verify_card = make_screen_card(
        SCREENSHOT_VERIFY,
        target_height=382,
        crop_top=74,
        crop_bottom=78,
    )
    exchange_card = make_screen_card(
        SCREENSHOT_EXCHANGE,
        target_height=456,
        crop_top=78,
        crop_bottom=90,
    )

    pedestal = Image.new("RGBA", CANVAS_SIZE, (0, 0, 0, 0))
    pedestal_draw = ImageDraw.Draw(pedestal)
    pedestal_draw.rounded_rectangle(
        (312, 346, 986, 488),
        radius=44,
        fill=(13, 27, 24, 132),
    )
    pedestal = pedestal.filter(ImageFilter.GaussianBlur(18))
    canvas.paste(pedestal, (0, 0), pedestal)

    place_card(canvas, security_card, (352, 74), rotation=-5)
    place_card(canvas, verify_card, (480, 112), rotation=5)
    place_card(canvas, exchange_card, (740, 22), rotation=0)

    return canvas


def create_left_hero_card(mark: Image.Image) -> Image.Image:
    size = (360, 420)
    card = Image.new("RGBA", size, (0, 0, 0, 0))

    glow(card, (-80, -40, 250, 230), "#19C98A", 90, 84)

    panel = Image.new("RGBA", size, (0, 0, 0, 0))
    panel_draw = ImageDraw.Draw(panel)
    panel_draw.rounded_rectangle(
        (0, 0, size[0], size[1]),
        radius=34,
        fill=(17, 33, 28, 228),
        outline=(72, 106, 96, 92),
        width=2,
    )
    card.alpha_composite(panel)

    logo = mark.resize((92, 92), Image.Resampling.LANCZOS)
    card.alpha_composite(logo, (24, 26))

    draw = ImageDraw.Draw(card)
    title_font = load_font(52, bold=True)
    subtitle_font = load_font(18, bold=False)
    pill_font = load_font(16, bold=True)
    small_font = load_font(15, bold=False)

    draw.text((24, 134), "PaySmart", font=title_font, fill=TEXT_PRIMARY)
    subtitle = "Secure identity, live FX and account control in one app."
    draw_multiline(
        draw,
        subtitle,
        (24, 202),
        width=24,
        font=subtitle_font,
        fill=TEXT_SECONDARY,
        line_gap=8,
    )

    pill_y = 306
    pill_x = 24
    for label in ("Passkeys", "Identity", "Live FX"):
        pill_width = text_width(draw, label, pill_font) + 34
        pill = Image.new("RGBA", (pill_width, 38), (0, 0, 0, 0))
        pill_draw = ImageDraw.Draw(pill)
        pill_draw.rounded_rectangle(
            (0, 0, pill.width, pill.height),
            radius=19,
            fill=(33, 63, 53, 235),
            outline=(110, 170, 149, 110),
            width=1,
        )
        pill_draw.text((17, 10), label, font=pill_font, fill=TEXT_PRIMARY)
        card.alpha_composite(pill, (pill_x, pill_y))
        pill_x += pill_width + 10

    draw.text(
        (24, 366),
        "Play feature graphic concept 1024 x 500",
        font=small_font,
        fill=(178, 201, 193),
    )
    return card


def save_logo_assets(mark: Image.Image) -> None:
    mark.save(LOGO_MARK_PNG, format="PNG", optimize=True)

    lockup = Image.new("RGBA", (1500, 480), (0, 0, 0, 0))
    mark_small = mark.resize((220, 220), Image.Resampling.LANCZOS)
    lockup.paste(mark_small, (80, 130), mark_small)
    draw = ImageDraw.Draw(lockup)
    name_font = load_font(168, bold=True)
    tagline_font = load_font(42, bold=False)
    draw.text((360, 110), "PaySmart", font=name_font, fill="#102822")
    draw.text(
        (366, 290),
        "Trusted payments. Confident access.",
        font=tagline_font,
        fill="#4A655C",
    )
    lockup.save(LOGO_LOCKUP_PNG, format="PNG", optimize=True)

    preview = Image.new("RGB", (1600, 900), "#EEF5F2")
    preview_draw = ImageDraw.Draw(preview)
    preview_draw.rounded_rectangle((60, 70, 770, 830), radius=42, fill="#F7FBF9")
    preview_draw.rounded_rectangle((830, 70, 1540, 830), radius=42, fill="#0B1714")
    preview_draw.text((108, 108), "Light background", font=load_font(38, True), fill="#17342D")
    preview_draw.text((878, 108), "Dark background", font=load_font(38, True), fill="#EEF5F2")
    light_mark = mark.resize((360, 360), Image.Resampling.LANCZOS)
    dark_mark = mark.resize((360, 360), Image.Resampling.LANCZOS)
    preview.paste(light_mark, (220, 248), light_mark)
    preview.paste(dark_mark, (990, 248), dark_mark)
    preview.save(LOGO_PREVIEW_PNG, format="PNG", optimize=True)

    LOGO_MARK_SVG.write_text(build_logo_svg(), encoding="utf-8")


def create_logo_mark(size: int) -> Image.Image:
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    center = size // 2
    outer_margin = int(size * 0.14)
    ring_width = int(size * 0.1)
    gap = int(size * 0.038)
    inner_radius = int(size * 0.19)

    glow_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow_layer)
    glow_draw.ellipse(
        (outer_margin - 20, outer_margin - 20, size - outer_margin + 20, size - outer_margin + 20),
        fill=(74, 232, 180, 130),
    )
    glow_layer = glow_layer.filter(ImageFilter.GaussianBlur(size * 0.055))
    img.alpha_composite(glow_layer)

    arc_box = (
        outer_margin,
        outer_margin,
        size - outer_margin,
        size - outer_margin,
    )
    draw.arc(arc_box, start=24, end=338, fill=ACCENT, width=ring_width)

    gap_mask = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    mask_draw = ImageDraw.Draw(gap_mask)
    mask_draw.ellipse(
        (
            size - outer_margin - ring_width - gap * 2,
            outer_margin - gap,
            size - outer_margin + gap,
            outer_margin + ring_width + gap * 2,
        ),
        fill=(0, 0, 0, 0),
    )

    dot_radius = int(size * 0.043)
    dot_box = (
        size - outer_margin - dot_radius * 2 + 8,
        outer_margin + ring_width // 3 - dot_radius + 6,
        size - outer_margin + 8,
        outer_margin + ring_width // 3 + dot_radius + 6,
    )
    draw.ellipse(dot_box, fill=ACCENT_SOFT)

    inner_box = (
        center - inner_radius,
        center - inner_radius,
        center + inner_radius,
        center + inner_radius,
    )
    draw.ellipse(inner_box, fill=ACCENT_DARK)

    check = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    check_draw = ImageDraw.Draw(check)
    stroke = int(size * 0.055)
    check_draw.line(
        [
            (center - int(size * 0.08), center + int(size * 0.005)),
            (center - int(size * 0.02), center + int(size * 0.075)),
            (center + int(size * 0.10), center - int(size * 0.065)),
        ],
        fill="white",
        width=stroke,
        joint="curve",
    )
    check = check.filter(ImageFilter.GaussianBlur(0.35))
    img.alpha_composite(check)

    return img


def build_logo_svg() -> str:
    return f"""<svg width="1024" height="1024" viewBox="0 0 1024 1024" fill="none" xmlns="http://www.w3.org/2000/svg">
  <circle cx="512" cy="512" r="370" fill="url(#glow)" opacity="0.55"/>
  <path d="M842 512C842 330 694 182 512 182C391 182 285 247 228 344" stroke="{ACCENT}" stroke-width="102" stroke-linecap="round"/>
  <path d="M230 346C198 399 182 454 182 512C182 694 330 842 512 842C616 842 710 793 771 716" stroke="{ACCENT}" stroke-width="102" stroke-linecap="round"/>
  <circle cx="512" cy="512" r="194" fill="{ACCENT_DARK}"/>
  <path d="M432 517L492 586L614 445" stroke="white" stroke-width="56" stroke-linecap="round" stroke-linejoin="round"/>
  <circle cx="791" cy="302" r="39" fill="{ACCENT_SOFT}"/>
  <defs>
    <radialGradient id="glow" cx="0" cy="0" r="1" gradientUnits="userSpaceOnUse" gradientTransform="translate(512 512) rotate(90) scale(370)">
      <stop stop-color="{ACCENT_SOFT}"/>
      <stop offset="1" stop-color="{ACCENT_SOFT}" stop-opacity="0"/>
    </radialGradient>
  </defs>
</svg>
"""


def make_screen_card(
    source_path: Path,
    target_height: int,
    crop_top: int = 0,
    crop_bottom: int = 0,
) -> Image.Image:
    base = Image.open(source_path).convert("RGBA")
    if crop_top or crop_bottom:
        base = base.crop((0, crop_top, base.width, base.height - crop_bottom))
    size = fit_size(base, target_height)
    content = base.resize(size, Image.Resampling.LANCZOS).filter(
        ImageFilter.UnsharpMask(radius=1.2, percent=190, threshold=2)
    )
    clean_top_strip(content)
    mask = rounded_mask(size, 28)
    content.putalpha(mask)

    card = Image.new("RGBA", size, (0, 0, 0, 0))
    card.paste(content, (0, 0), content)

    border = Image.new("RGBA", size, (0, 0, 0, 0))
    border_draw = ImageDraw.Draw(border)
    border_draw.rounded_rectangle(
        (0, 0, size[0] - 1, size[1] - 1),
        radius=28,
        outline=(220, 235, 229, 62),
        width=2,
    )
    card.alpha_composite(border)
    return card


def clean_top_strip(image: Image.Image) -> None:
    draw = ImageDraw.Draw(image)
    sample_color = image.getpixel((12, 12))
    draw.rectangle((0, 0, image.width, 18), fill=sample_color)


def place_card(canvas: Image.Image, card: Image.Image, position: tuple[int, int], rotation: int) -> None:
    shadow = Image.new("RGBA", card.size, (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    shadow_draw.rounded_rectangle(
        (12, 16, card.width - 8, card.height - 4),
        radius=28,
        fill=(0, 0, 0, 140),
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(18))

    if rotation:
        card = card.rotate(rotation, resample=Image.Resampling.BICUBIC, expand=True)
        shadow = shadow.rotate(rotation, resample=Image.Resampling.BICUBIC, expand=True)

    canvas.paste(shadow, (position[0] + 6, position[1] + 10), shadow)
    canvas.paste(card, position, card)


def fit_size(image: Image.Image, target_height: int) -> tuple[int, int]:
    ratio = image.width / image.height
    target_width = int(target_height * ratio)
    return target_width, target_height


def rounded_mask(size: tuple[int, int], radius: int) -> Image.Image:
    mask = Image.new("L", size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle((0, 0, size[0], size[1]), radius=radius, fill=255)
    return mask


def glow(canvas: Image.Image, box: tuple[int, int, int, int], color: str, blur: int, alpha: int) -> None:
    overlay = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    rgba = hex_to_rgba(color, alpha)
    draw.ellipse(box, fill=rgba)
    overlay = overlay.filter(ImageFilter.GaussianBlur(blur))
    canvas.paste(overlay, (0, 0), overlay)


def draw_linear_gradient(canvas: Image.Image, top_hex: str, bottom_hex: str) -> None:
    top = Image.new("RGB", canvas.size, top_hex)
    bottom = Image.new("RGB", canvas.size, bottom_hex)
    mask = Image.linear_gradient("L").resize((1, canvas.height)).resize(canvas.size)
    blended = Image.composite(bottom, top, mask)
    canvas.paste(blended)


def draw_multiline(
    draw: ImageDraw.ImageDraw,
    text: str,
    position: tuple[int, int],
    width: int,
    font: ImageFont.FreeTypeFont,
    fill: str,
    line_gap: int,
) -> None:
    y = position[1]
    for line in wrap(text, width=width):
        draw.text((position[0], y), line, font=font, fill=fill)
        y += font.size + line_gap


def text_width(draw: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont) -> int:
    bbox = draw.textbbox((0, 0), text, font=font)
    return bbox[2] - bbox[0]


def load_font(size: int, bold: bool) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates = []
    if bold:
        candidates.extend(
            [
                r"C:\Windows\Fonts\segoeuib.ttf",
                r"C:\Windows\Fonts\arialbd.ttf",
            ]
        )
    else:
        candidates.extend(
            [
                r"C:\Windows\Fonts\segoeui.ttf",
                r"C:\Windows\Fonts\arial.ttf",
            ]
        )

    for candidate in candidates:
        path = Path(candidate)
        if path.exists():
            return ImageFont.truetype(str(path), size=size)

    return ImageFont.load_default()


def hex_to_rgba(value: str, alpha: int) -> tuple[int, int, int, int]:
    value = value.lstrip("#")
    return tuple(int(value[i : i + 2], 16) for i in (0, 2, 4)) + (alpha,)


if __name__ == "__main__":
    main()
