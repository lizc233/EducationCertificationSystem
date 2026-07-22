from PIL import Image, ImageDraw, ImageFont


WIDTH = 3000
HEIGHT = 1900
BG = "white"
OUT_FILE = "成员E用例图.png"


def load_font(path, size):
    try:
        return ImageFont.truetype(path, size)
    except OSError:
        return ImageFont.load_default()


TITLE_FONT = load_font(r"C:\Windows\Fonts\msyhbd.ttc", 34)
TEXT_FONT = load_font(r"C:\Windows\Fonts\msyh.ttc", 22)
SMALL_FONT = load_font(r"C:\Windows\Fonts\msyh.ttc", 16)
UC_FONT = load_font(r"C:\Windows\Fonts\msyhbd.ttc", 20)


def center_text(draw, xy, text, font, fill="black"):
    bbox = draw.textbbox((0, 0), text, font=font)
    w = bbox[2] - bbox[0]
    h = bbox[3] - bbox[1]
    draw.text((xy[0] - w / 2, xy[1] - h / 2), text, font=font, fill=fill)


def draw_actor(draw, x, y, label):
    r = 18
    draw.ellipse((x - r, y - 80, x + r, y - 44), outline="black", width=3)
    draw.line((x, y - 44, x, y + 20), fill="black", width=3)
    draw.line((x - 32, y - 8, x + 32, y - 8), fill="black", width=3)
    draw.line((x, y + 20, x - 28, y + 65), fill="black", width=3)
    draw.line((x, y + 20, x + 28, y + 65), fill="black", width=3)
    center_text(draw, (x, y + 100), label, TEXT_FONT)
    return (x, y - 8)


def draw_usecase(draw, center, text, fill="#BDF5E8", w=210, h=82):
    x, y = center
    box = (x - w // 2, y - h // 2, x + w // 2, y + h // 2)
    draw.ellipse(box, fill=fill, outline="black", width=3)
    center_text(draw, (x, y), text, UC_FONT)
    return {"center": center, "box": box, "w": w, "h": h}


def draw_assoc(draw, p1, p2, width=3):
    draw.line((p1, p2), fill="black", width=width)


def ellipse_edge(uc, target):
    x, y = uc["center"]
    tx, ty = target
    dx = tx - x
    dy = ty - y
    if dx == 0 and dy == 0:
        return x, y
    a = uc["w"] / 2
    b = uc["h"] / 2
    scale = ((dx * dx) / (a * a) + (dy * dy) / (b * b)) ** 0.5
    return x + dx / scale, y + dy / scale


def draw_dashed_arrow(draw, start, end, label=None, label_offset=(0, -18)):
    dash = 12
    gap = 8
    x1, y1 = start
    x2, y2 = end
    dx = x2 - x1
    dy = y2 - y1
    dist = (dx * dx + dy * dy) ** 0.5
    if dist == 0:
        return
    ux = dx / dist
    uy = dy / dist
    pos = 0
    while pos < dist - dash:
        sx = x1 + ux * pos
        sy = y1 + uy * pos
        ex = x1 + ux * min(pos + dash, dist)
        ey = y1 + uy * min(pos + dash, dist)
        draw.line((sx, sy, ex, ey), fill="black", width=2)
        pos += dash + gap

    arrow_len = 18
    arrow_w = 8
    bx = x2 - ux * arrow_len
    by = y2 - uy * arrow_len
    px = -uy
    py = ux
    draw.line((bx, by, x2, y2), fill="black", width=2)
    draw.line((bx + px * arrow_w, by + py * arrow_w, x2, y2), fill="black", width=2)
    draw.line((bx - px * arrow_w, by - py * arrow_w, x2, y2), fill="black", width=2)

    if label:
        mx = (x1 + x2) / 2
        my = (y1 + y2) / 2
        center_text(draw, (mx + label_offset[0], my + label_offset[1]), label, SMALL_FONT)


img = Image.new("RGB", (WIDTH, HEIGHT), BG)
draw = ImageDraw.Draw(img)

draw.rounded_rectangle((280, 100, 2720, 1720), radius=16, outline="black", width=3)
center_text(draw, (1500, 70), "成员E 用例图", TITLE_FONT)
center_text(draw, (1500, 125), "调查与评价、持续改进、自评报告、AI辅助", TEXT_FONT)

# Actors
admin = draw_actor(draw, 90, 340, "管理员")
respondent = draw_actor(draw, 90, 850, "受访用户")
teacher = draw_actor(draw, 90, 1390, "教师/专业负责人")
notice = draw_actor(draw, 2870, 610, "通知中心")
ai_service = draw_actor(draw, 2870, 1380, "AI服务")

# Use cases
uc = {}
uc["create_questionnaire"] = draw_usecase(draw, (650, 260), "设计问卷")
uc["manage_questionnaire"] = draw_usecase(draw, (1050, 260), "维护题目")
uc["preview_questionnaire"] = draw_usecase(draw, (1450, 260), "预览问卷")
uc["publish_questionnaire"] = draw_usecase(draw, (1850, 260), "发布问卷")
uc["push_notice"] = draw_usecase(draw, (2250, 260), "推送填报通知")

uc["fill_questionnaire"] = draw_usecase(draw, (650, 720), "在线填报问卷")
uc["validate_required"] = draw_usecase(draw, (1050, 720), "校验必答项")
uc["prevent_repeat"] = draw_usecase(draw, (1450, 720), "防重复提交")
uc["response_stats"] = draw_usecase(draw, (1850, 720), "回收统计分析")
uc["export_response"] = draw_usecase(draw, (2250, 720), "导出答卷/统计")

uc["create_improve_plan"] = draw_usecase(draw, (650, 1180), "制定改进计划")
uc["assign_action"] = draw_usecase(draw, (1050, 1180), "分解改进行动")
uc["record_improve"] = draw_usecase(draw, (1450, 1180), "记录执行过程")
uc["track_status"] = draw_usecase(draw, (1850, 1180), "跟踪状态与到期")
uc["close_plan"] = draw_usecase(draw, (2250, 1180), "复核并关闭改进单")

uc["create_report"] = draw_usecase(draw, (650, 1580), "创建报告项目")
uc["assign_chapter"] = draw_usecase(draw, (1050, 1580), "分配章节任务")
uc["edit_report"] = draw_usecase(draw, (1450, 1580), "协同撰写章节")
uc["view_progress"] = draw_usecase(draw, (1850, 1580), "查看进度看板")
uc["ai_assist"] = draw_usecase(draw, (2250, 1580), "AI分析/润色/扩写")

# Associations
for key in ["create_questionnaire", "manage_questionnaire", "preview_questionnaire", "publish_questionnaire", "response_stats", "export_response"]:
    draw_assoc(draw, admin, ellipse_edge(uc[key], admin))

for key in ["fill_questionnaire"]:
    draw_assoc(draw, respondent, ellipse_edge(uc[key], respondent))

for key in ["create_improve_plan", "assign_action", "record_improve", "track_status", "close_plan",
            "create_report", "assign_chapter", "edit_report", "view_progress", "ai_assist"]:
    draw_assoc(draw, teacher, ellipse_edge(uc[key], teacher))

for key in ["push_notice", "track_status"]:
    draw_assoc(draw, notice, ellipse_edge(uc[key], notice))

draw_assoc(draw, ai_service, ellipse_edge(uc["ai_assist"], ai_service))

# Include / extend
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["manage_questionnaire"], uc["create_questionnaire"]["center"]),
    ellipse_edge(uc["create_questionnaire"], uc["manage_questionnaire"]["center"]),
    "<<include>>",
    (-10, -20),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["preview_questionnaire"], uc["create_questionnaire"]["center"]),
    ellipse_edge(uc["create_questionnaire"], uc["preview_questionnaire"]["center"]),
    "<<extend>>",
    (42, -44),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["push_notice"], uc["publish_questionnaire"]["center"]),
    ellipse_edge(uc["publish_questionnaire"], uc["push_notice"]["center"]),
    "<<include>>",
    (0, -20),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["validate_required"], uc["fill_questionnaire"]["center"]),
    ellipse_edge(uc["fill_questionnaire"], uc["validate_required"]["center"]),
    "<<include>>",
    (-12, -20),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["prevent_repeat"], uc["fill_questionnaire"]["center"]),
    ellipse_edge(uc["fill_questionnaire"], uc["prevent_repeat"]["center"]),
    "<<include>>",
    (8, -22),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["export_response"], uc["response_stats"]["center"]),
    ellipse_edge(uc["response_stats"], uc["export_response"]["center"]),
    "<<extend>>",
    (0, -20),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["assign_action"], uc["create_improve_plan"]["center"]),
    ellipse_edge(uc["create_improve_plan"], uc["assign_action"]["center"]),
    "<<include>>",
    (-10, -20),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["record_improve"], uc["create_improve_plan"]["center"]),
    ellipse_edge(uc["create_improve_plan"], uc["record_improve"]["center"]),
    "<<extend>>",
    (10, -24),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["close_plan"], uc["track_status"]["center"]),
    ellipse_edge(uc["track_status"], uc["close_plan"]["center"]),
    "<<extend>>",
    (0, -20),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["assign_chapter"], uc["create_report"]["center"]),
    ellipse_edge(uc["create_report"], uc["assign_chapter"]["center"]),
    "<<include>>",
    (-10, -20),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["edit_report"], uc["assign_chapter"]["center"]),
    ellipse_edge(uc["assign_chapter"], uc["edit_report"]["center"]),
    "<<include>>",
    (0, -20),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["view_progress"], uc["edit_report"]["center"]),
    ellipse_edge(uc["edit_report"], uc["view_progress"]["center"]),
    "<<extend>>",
    (0, -42),
)
draw_dashed_arrow(
    draw,
    ellipse_edge(uc["ai_assist"], uc["edit_report"]["center"]),
    ellipse_edge(uc["edit_report"], uc["ai_assist"]["center"]),
    "<<extend>>",
    (8, -24),
)

img.save(OUT_FILE)
print(f"Saved {OUT_FILE}")
