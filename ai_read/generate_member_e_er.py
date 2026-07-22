from PIL import Image, ImageDraw, ImageFont


WIDTH = 3800
HEIGHT = 2350
BG = "white"

OUT_FILE = "ER_MemberE_Core.png"


def load_font(path, size):
    try:
        return ImageFont.truetype(path, size)
    except OSError:
        return ImageFont.load_default()


TITLE_FONT = load_font(r"C:\Windows\Fonts\arialbd.ttf", 38)
SECTION_FONT = load_font(r"C:\Windows\Fonts\arialbd.ttf", 26)
ENTITY_FONT = load_font(r"C:\Windows\Fonts\arialbd.ttf", 22)
TEXT_FONT = load_font(r"C:\Windows\Fonts\arial.ttf", 18)
REL_FONT = load_font(r"C:\Windows\Fonts\arialbd.ttf", 18)
CARD_FONT = load_font(r"C:\Windows\Fonts\arialbd.ttf", 20)


def center_text(draw, xy, text, font, fill="black"):
    bbox = draw.textbbox((0, 0), text, font=font)
    w = bbox[2] - bbox[0]
    h = bbox[3] - bbox[1]
    draw.text((xy[0] - w / 2, xy[1] - h / 2), text, font=font, fill=fill)


def draw_entity(draw, center, name, attrs):
    x, y = center
    w, h = 280, 72
    left = x - w // 2
    top = y - h // 2
    right = x + w // 2
    bottom = y + h // 2

    draw.rounded_rectangle(
        (left, top, right, bottom),
        radius=8,
        fill="#FFF4D6",
        outline="black",
        width=3,
    )
    center_text(draw, (x, y), name, ENTITY_FONT)

    oval_w, oval_h = 188, 44
    left_attrs = attrs[::2]
    right_attrs = attrs[1::2]

    def draw_attr_group(group, side):
        count = len(group)
        if count == 0:
            return
        start_y = y - (count - 1) * 28
        for idx, attr in enumerate(group):
            ay = start_y + idx * 56
            ax = x - 250 if side == "left" else x + 250
            draw.ellipse(
                (ax - oval_w // 2, ay - oval_h // 2, ax + oval_w // 2, ay + oval_h // 2),
                fill="#EAF5FF",
                outline="black",
                width=2,
            )
            center_text(draw, (ax, ay), attr, TEXT_FONT)
            line_start = (left, y) if side == "left" else (right, y)
            line_end = (ax - oval_w // 2, ay) if side == "right" else (ax + oval_w // 2, ay)
            draw.line((line_start, line_end), fill="black", width=2)

    draw_attr_group(left_attrs, "left")
    draw_attr_group(right_attrs, "right")

    return {
        "center": center,
        "left": left,
        "right": right,
        "top": top,
        "bottom": bottom,
        "width": w,
        "height": h,
    }


def draw_diamond(draw, center, text):
    x, y = center
    rw, rh = 95, 58
    points = [(x, y - rh), (x + rw, y), (x, y + rh), (x - rw, y)]
    draw.polygon(points, fill="#FBE7E7", outline="black")
    center_text(draw, (x, y), text, REL_FONT)


def edge_point(box, target):
    x, y = box["center"]
    tx, ty = target
    dx = tx - x
    dy = ty - y
    if dx == 0 and dy == 0:
        return x, y
    half_w = box["width"] / 2
    half_h = box["height"] / 2
    scale_x = abs(dx) / half_w if half_w else 0
    scale_y = abs(dy) / half_h if half_h else 0
    if scale_x > scale_y:
        px = x + (half_w if dx > 0 else -half_w)
        py = y + dy / scale_x
    else:
        px = x + dx / scale_y
        py = y + (half_h if dy > 0 else -half_h)
    return px, py


def draw_relationship(draw, box_a, box_b, diamond_center, label, card_a="1", card_b="N"):
    draw_diamond(draw, diamond_center, label)
    a_point = edge_point(box_a, diamond_center)
    b_point = edge_point(box_b, diamond_center)
    draw.line((a_point, diamond_center), fill="black", width=3)
    draw.line((diamond_center, b_point), fill="black", width=3)

    ax = a_point[0] + (diamond_center[0] - a_point[0]) * 0.18
    ay = a_point[1] + (diamond_center[1] - a_point[1]) * 0.18
    bx = b_point[0] + (diamond_center[0] - b_point[0]) * 0.18
    by = b_point[1] + (diamond_center[1] - b_point[1]) * 0.18
    center_text(draw, (ax, ay - 18), card_a, CARD_FONT)
    center_text(draw, (bx, by - 18), card_b, CARD_FONT)


img = Image.new("RGB", (WIDTH, HEIGHT), BG)
draw = ImageDraw.Draw(img)

# Module backgrounds
sections = [
    ((60, 120, 1960, 980), "#F6FAFF", "Survey Module (F21-F22)"),
    ((60, 1120, 1960, 1820), "#F8FFF5", "Improvement Module (F23)"),
    ((2100, 120, 3720, 980), "#FFF9F2", "Report Module (F24)"),
    ((2100, 1120, 3720, 2120), "#FFF7FB", "AI Module (F25)"),
]

for (left, top, right, bottom), color, title in sections:
    draw.rounded_rectangle((left, top, right, bottom), radius=18, fill=color, outline="#C7C7C7", width=2)
    center_text(draw, ((left + right) / 2, top + 26), title, SECTION_FONT)

center_text(draw, (WIDTH / 2, 55), "ER Diagram for Member E (Core Tables)", TITLE_FONT)
center_text(draw, (WIDTH / 2, 92), "Table names and attributes are shown in English; relationships are marked as 1:1 or 1:N.", TEXT_FONT)

entities = {}

entities["survey_questionnaire"] = draw_entity(
    draw,
    (420, 300),
    "survey_questionnaire",
    ["questionnaire_code", "title", "target_object_type", "publish_status", "start_time"],
)
entities["survey_question"] = draw_entity(
    draw,
    (1130, 300),
    "survey_question",
    ["question_code", "question_text", "question_type", "is_required"],
)
entities["survey_question_option"] = draw_entity(
    draw,
    (1820, 300),
    "survey_question_option",
    ["option_code", "option_text", "option_value", "sort_no"],
)
entities["survey_response"] = draw_entity(
    draw,
    (420, 760),
    "survey_response",
    ["respondent_user_id", "respondent_type", "submit_status", "submitted_at"],
)
entities["survey_publish_task"] = draw_entity(
    draw,
    (1130, 760),
    "survey_publish_task",
    ["publish_batch_no", "publish_status", "mq_status", "retry_count"],
)
entities["survey_response_answer"] = draw_entity(
    draw,
    (1820, 760),
    "survey_response_answer",
    ["question_id", "option_id", "answer_text", "answer_json"],
)

entities["improve_plan"] = draw_entity(
    draw,
    (420, 1410),
    "improve_plan",
    ["plan_code", "source_type", "target_type", "due_date", "status"],
)
entities["improve_plan_action"] = draw_entity(
    draw,
    (1130, 1410),
    "improve_plan_action",
    ["action_code", "action_title", "responsible_user_id", "progress_percent", "status"],
)
entities["improve_plan_record"] = draw_entity(
    draw,
    (1820, 1410),
    "improve_plan_record",
    ["action_id", "record_type", "record_time", "attachment_file_id"],
)

entities["report_project"] = draw_entity(
    draw,
    (2460, 300),
    "report_project",
    ["report_code", "project_name", "academic_year", "generation_mode", "status"],
)
entities["report_chapter"] = draw_entity(
    draw,
    (3250, 300),
    "report_chapter",
    ["chapter_code", "chapter_title", "source_type", "chapter_status"],
)
entities["report_task_assignment"] = draw_entity(
    draw,
    (3250, 760),
    "report_task_assignment",
    ["assignee_user_id", "role_type", "due_date", "assignment_status"],
)
entities["report_draft"] = draw_entity(
    draw,
    (2460, 760),
    "report_draft",
    ["version_no", "draft_content", "edited_by", "edited_at"],
)

entities["ai_prompt_template"] = draw_entity(
    draw,
    (2460, 1410),
    "ai_prompt_template",
    ["template_code", "scenario_type", "system_prompt", "enabled"],
)
entities["ai_analysis_request"] = draw_entity(
    draw,
    (3250, 1410),
    "ai_analysis_request",
    ["request_no", "source_type", "template_id", "request_status", "requester_user_id"],
)
entities["ai_analysis_result"] = draw_entity(
    draw,
    (2855, 1910),
    "ai_analysis_result",
    ["request_id", "result_type", "result_text", "human_confirmed_flag"],
)

relationships = [
    ("survey_questionnaire", "survey_question", (775, 300), "contains", "1", "N"),
    ("survey_question", "survey_question_option", (1475, 300), "offers", "1", "N"),
    ("survey_questionnaire", "survey_response", (420, 530), "collects", "1", "N"),
    ("survey_questionnaire", "survey_publish_task", (775, 615), "publishes", "1", "N"),
    ("survey_response", "survey_response_answer", (1120, 760), "submits", "1", "N"),
    ("survey_question", "survey_response_answer", (1475, 535), "answers", "1", "N"),
    ("improve_plan", "improve_plan_action", (775, 1410), "breaks_down", "1", "N"),
    ("improve_plan_action", "improve_plan_record", (1475, 1410), "records", "1", "N"),
    ("report_project", "report_chapter", (2855, 300), "contains", "1", "N"),
    ("report_chapter", "report_task_assignment", (3250, 535), "assigns", "1", "N"),
    ("report_chapter", "report_draft", (2855, 760), "drafts", "1", "N"),
    ("ai_prompt_template", "ai_analysis_request", (2855, 1410), "drives", "1", "N"),
    ("ai_analysis_request", "ai_analysis_result", (3050, 1670), "returns", "1", "1"),
    ("report_chapter", "ai_analysis_request", (3440, 980), "sources", "1", "N"),
]

for left_name, right_name, diamond, label, card_a, card_b in relationships:
    draw_relationship(draw, entities[left_name], entities[right_name], diamond, label, card_a, card_b)

note_lines = [
    "Scope selection, user, file, and notification support tables are omitted",
    "to keep the ER image readable and focused on Member E core tables.",
]
center_text(draw, (960, 1875), note_lines[0], TEXT_FONT, fill="#444444")
center_text(draw, (960, 1905), note_lines[1], TEXT_FONT, fill="#444444")

img.save(OUT_FILE)
print(f"Saved {OUT_FILE}")
