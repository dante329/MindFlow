#!/usr/bin/env python3
"""Generate MindFlow project design document based on template."""

import sys
import os

# Use bundled python-docx
sys.path.insert(0, '/Users/dante/.dsh/dsh-runtimes/dsh-primary-runtime/dependencies/python/lib/python3.12/site-packages')

from docx import Document
from docx.shared import Pt, Cm, Emu, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn, nsdecls
from docx.oxml import OxmlElement, parse_xml
from copy import deepcopy

TEMPLATE_PATH = '/Users/dante/Library/Containers/com.tencent.xinWeChat/Data/Documents/xwechat_files/wxid_qi4z1okexh7f19_f058/temp/drag/02 HiGo电子商务网站系统设计说明书(1).docx'
OUTPUT_PATH = '/Users/dante/Code/Java/MindFlow/G01-MindFlow考研复习系统设计文档.docx'
SRC_DIR = '/Users/dante/Code/Java/MindFlow/src'

# ============================================================
# Helper functions
# ============================================================

def set_run_font(run, font_name='宋体', font_size=Pt(10.5), bold=False):
    """Set both ASCII and eastAsia font for a run."""
    run.font.name = font_name
    run.font.size = font_size
    run.bold = bold
    run.font.color.rgb = RGBColor(0, 0, 0)
    rpr = run._element.get_or_add_rPr()
    rfonts = rpr.find(qn('w:rFonts'))
    if rfonts is None:
        rfonts = run._element.makeelement(qn('w:rFonts'), {})
        rpr.insert(0, rfonts)
    rfonts.set(qn('w:eastAsia'), font_name)
    rfonts.set(qn('w:ascii'), font_name)
    rfonts.set(qn('w:hAnsi'), font_name)


def add_body_paragraph(doc, text, bold=False, first_line_indent=True):
    """Add a paragraph using '我的正文' style with proper font."""
    p = doc.add_paragraph(style='我的正文')
    run = p.add_run(text)
    set_run_font(run, '宋体', Pt(12), bold=bold)
    if first_line_indent:
        pf = p.paragraph_format
        pf.first_line_indent = Cm(0.74)
    return p


def add_heading(doc, text, level=1):
    """Add a heading with proper eastAsia font."""
    h = doc.add_heading(text, level=level)
    for run in h.runs:
        run.font.color.rgb = RGBColor(0, 0, 0)
        rpr = run._element.get_or_add_rPr()
        rfonts = rpr.find(qn('w:rFonts'))
        if rfonts is None:
            rfonts = run._element.makeelement(qn('w:rFonts'), {})
            rpr.insert(0, rfonts)
        rfonts.set(qn('w:eastAsia'), '黑体')
    return h


def add_code_block(doc, code_text):
    """Add code as individual paragraphs with Consolas 10pt."""
    lines = code_text.split('\n')
    for line in lines:
        p = doc.add_paragraph()
        run = p.add_run(line if line else ' ')
        run.font.name = 'Consolas'
        run.font.size = Pt(9)
        rpr = run._element.get_or_add_rPr()
        rfonts = rpr.find(qn('w:rFonts'))
        if rfonts is None:
            rfonts = run._element.makeelement(qn('w:rFonts'), {})
            rpr.insert(0, rfonts)
        rfonts.set(qn('w:eastAsia'), 'Consolas')
        rfonts.set(qn('w:ascii'), 'Consolas')
        rfonts.set(qn('w:hAnsi'), 'Consolas')
        # Set spacing to tight
        pf = p.paragraph_format
        pf.space_before = Pt(0)
        pf.space_after = Pt(0)
        pf.line_spacing = Pt(12)


def set_cell_border(cell, **kwargs):
    """Set borders on a table cell."""
    tc = cell._tc
    tcPr = tc.get_or_add_tcPr()
    tcBorders = tcPr.find(qn('w:tcBorders'))
    if tcBorders is None:
        tcBorders = parse_xml(f'<w:tcBorders {nsdecls("w")}/>')
        tcPr.append(tcBorders)
    for edge, val in kwargs.items():
        element = tcBorders.find(qn(f'w:{edge}'))
        if element is None:
            element = parse_xml(f'<w:{edge} {nsdecls("w")}/>')
            tcBorders.append(element)
        for key, v in val.items():
            element.set(qn(f'w:{key}'), str(v))


def set_table_borders(table):
    """Set borders on all cells of a table."""
    border_args = {
        'top': {'val': 'single', 'sz': '4', 'space': '0', 'color': '000000'},
        'bottom': {'val': 'single', 'sz': '4', 'space': '0', 'color': '000000'},
        'start': {'val': 'single', 'sz': '4', 'space': '0', 'color': '000000'},
        'end': {'val': 'single', 'sz': '4', 'space': '0', 'color': '000000'},
    }
    for row in table.rows:
        for cell in row.cells:
            set_cell_border(cell, **border_args)


def add_table_with_data(doc, headers, rows, col_widths=None):
    """Add a formatted table with headers and data rows."""
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.style = 'Normal Table'
    
    # Header row
    for i, header in enumerate(headers):
        cell = table.rows[0].cells[i]
        cell.text = ''
        p = cell.paragraphs[0]
        run = p.add_run(header)
        set_run_font(run, '宋体', Pt(10.5), bold=True)
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    
    # Data rows
    for r_idx, row_data in enumerate(rows):
        for c_idx, val in enumerate(row_data):
            cell = table.rows[r_idx + 1].cells[c_idx]
            cell.text = ''
            p = cell.paragraphs[0]
            run = p.add_run(str(val))
            set_run_font(run, '宋体', Pt(10.5))
    
    set_table_borders(table)
    return table


def add_toc(doc):
    """Insert a TOC field code."""
    paragraph = doc.add_paragraph()
    run = paragraph.add_run()
    fldChar1 = OxmlElement('w:fldChar')
    fldChar1.set(qn('w:fldCharType'), 'begin')
    instrText = OxmlElement('w:instrText')
    instrText.set(qn('xml:space'), 'preserve')
    instrText.text = 'TOC \\o "1-3" \\h \\z \\u'
    fldChar2 = OxmlElement('w:fldChar')
    fldChar2.set(qn('w:fldCharType'), 'separate')
    fldChar3 = OxmlElement('w:t')
    fldChar3.text = '（右键点击此处，选择\u201c更新域\u201d以生成目录）'
    fldChar4 = OxmlElement('w:fldChar')
    fldChar4.set(qn('w:fldCharType'), 'end')
    run._r.append(fldChar1)
    run._r.append(instrText)
    run._r.append(fldChar2)
    run._r.append(fldChar3)
    run._r.append(fldChar4)


def read_source(rel_path):
    """Read a Java source file."""
    full = os.path.join(SRC_DIR, rel_path)
    with open(full, 'r', encoding='utf-8') as f:
        return f.read()


# ============================================================
# Main generation
# ============================================================

def main():
    print("Loading template...")
    doc = Document(TEMPLATE_PATH)
    
    # Clear body but preserve sectPr
    body = doc.element.body
    sectPr = body.find(qn('w:sectPr'))
    children_to_remove = [child for child in body if child.tag != qn('w:sectPr')]
    for child in children_to_remove:
        body.remove(child)
    
    print("Template cleared. Building document...")
    
    # ========== COVER PAGE ==========
    # Title
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    pf = p.paragraph_format
    pf.space_before = Pt(120)
    run = p.add_run('MindFlow考研备考复习系统')
    set_run_font(run, '黑体', Pt(26), bold=True)
    
    p2 = doc.add_paragraph()
    p2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run2 = p2.add_run('项目设计说明书')
    set_run_font(run2, '黑体', Pt(22), bold=True)
    
    # Blank lines
    for _ in range(6):
        doc.add_paragraph()
    
    # Info table on cover
    info_items = [
        ('项目名称', 'MindFlow考研备考复习系统'),
        ('文档编号', 'MF-DESIGN-001'),
        ('版本号', 'V1.0'),
        ('编写日期', '2025年1月'),
        ('编写人员', 'MindFlow开发团队'),
    ]
    for label, value in info_items:
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = p.add_run(f'{label}：{value}')
        set_run_font(run, '宋体', Pt(14))
    
    doc.add_page_break()
    
    # ========== TOC ==========
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run('目    录')
    set_run_font(run, '黑体', Pt(16), bold=True)
    
    add_toc(doc)
    doc.add_page_break()
    
    # ========== CHAPTER 1: 引言 ==========
    add_heading(doc, '1 引言', level=1)
    
    add_heading(doc, '1.1 编写目的', level=2)
    add_body_paragraph(doc, '本文档是MindFlow考研备考复习系统的项目设计说明书，旨在详细描述系统的整体架构设计、功能模块设计、数据库设计以及安全设计等方面的内容。本文档面向的读者包括项目开发团队成员、测试人员、项目管理人员以及相关评审专家。通过阅读本文档，各方人员能够全面了解系统的技术实现方案，为后续的开发、测试和维护工作提供明确的技术指导和参考依据。')
    add_body_paragraph(doc, '本设计说明书基于需求分析阶段的成果，将用户需求转化为具体的技术方案，明确了各功能模块的接口定义、数据流转方式、核心算法逻辑以及数据库表结构设计。同时，文档中对系统采用的设计模式进行了详细说明，确保系统在可扩展性、可维护性和代码复用方面达到预期目标。')
    
    add_heading(doc, '1.2 背景', level=2)
    add_body_paragraph(doc, '随着考研竞争日益激烈，考生对高效、科学的备考工具需求不断增长。传统的纸质资料和简单的刷题App已难以满足考生个性化、系统化复习的需求。MindFlow考研备考复习系统正是在这一背景下开发的，它致力于为广大考研学子提供一个集题库管理、智能练习、错题追踪、学习统计和科学复习提醒于一体的综合性备考平台。')
    add_body_paragraph(doc, '该系统采用Java语言开发，基于控制台交互界面，使用SQLite作为嵌入式数据库，具有部署简单、运行轻量、数据安全等特点。系统核心功能涵盖用户管理、科目管理、题库管理、答题练习、错题本、每日计划、学习统计和复习提醒八大模块，并运用了工厂模式、策略模式、模板方法模式等多种面向对象设计模式，确保了代码的高内聚低耦合。')
    add_body_paragraph(doc, 'MindFlow系统特别引入了艾宾浩斯遗忘曲线理论来指导复习节奏，通过多线程后台定时任务自动推送复习提醒，帮助考生在最佳时间节点进行复习巩固，从而显著提升记忆效率和备考效果。')
    
    add_heading(doc, '1.3 参考资料', level=2)
    refs = [
        '《软件工程导论》（第6版），张海藩，人民邮电出版社',
        '《Java核心技术》（第12版），Cay S. Horstmann，机械工业出版社',
        '《Head First设计模式》，Eric Freeman等，中国电力出版社',
        '《数据库系统概论》（第6版），王珊、萨师煊，高等教育出版社',
        'MindFlow考研备考复习系统需求规格说明书',
        'SQLite官方文档 (https://www.sqlite.org/docs.html)',
        '艾宾浩斯遗忘曲线相关研究文献',
    ]
    for ref in refs:
        add_body_paragraph(doc, f'• {ref}', first_line_indent=False)
    
    add_heading(doc, '1.4 术语定义', level=2)
    terms = [
        ['MindFlow', '本系统的名称，寓意"思维流动"，代表高效流畅的学习体验'],
        ['科目', '考研考试科目，如政治、英语、数学、专业课等'],
        ['知识点', '科目下的知识单元，支持树形层级结构'],
        ['题型', '题目类型，包括选择题(CHOICE)、判断题(TRUE_FALSE)、填空题(FILL_BLANK)'],
        ['错题本', '记录用户答错题目的功能模块，包含错误次数和掌握程度'],
        ['掌握程度', '错题的熟练度等级：未掌握(0)、部分掌握(1)、基本掌握(2)、已掌握(3)'],
        ['艾宾浩斯复习', '基于艾宾浩斯遗忘曲线的7轮复习提醒机制(1/2/4/7/15/30/90天)'],
        ['学习会话', '一次连续的学习时间段记录，包含开始时间、结束时间和做题数量'],
        ['GradingStrategy', '判分策略接口，使用策略模式处理不同题型的评分逻辑'],
        ['QuestionFactory', '题目工厂类，使用工厂模式创建不同类型的题目对象'],
        ['DAO', 'Data Access Object，数据访问对象，封装数据库CRUD操作'],
        ['SQLite', '轻量级嵌入式关系数据库，无需独立服务器进程'],
    ]
    add_table_with_data(doc, ['术语', '定义'], terms)
    
    doc.add_page_break()
    
    # ========== CHAPTER 2: 设计概述 ==========
    add_heading(doc, '2 设计概述', level=1)
    
    add_heading(doc, '2.1 任务和目标', level=2)
    
    add_heading(doc, '2.1.1 需求概述', level=3)
    add_body_paragraph(doc, 'MindFlow考研备考复习系统需要满足以下核心需求：')
    add_body_paragraph(doc, '（1）用户管理需求：系统需要提供用户注册、登录、登出和个人信息管理功能。用户注册时需要校验用户名和密码的有效性，密码需经过哈希加密存储以确保安全性。新用户注册后系统自动初始化政治、英语、数学、专业课四个考研预设科目。')
    add_body_paragraph(doc, '（2）科目管理需求：用户可以查看、添加、修改和删除自定义科目。预设科目不允许删除但可以修改描述和总分。每个科目下可以管理知识点，支持树形层级结构。')
    add_body_paragraph(doc, '（3）题库管理需求：系统支持选择题、判断题、填空题三种题型的增删改查操作。题目可以关联知识点，设置难度等级（1-5星），并提供解析信息。支持按关键词搜索题目。')
    add_body_paragraph(doc, '（4）答题练习需求：提供随机练习、错题重做、按题型练习三种模式。答题过程实时计时，使用策略模式对不同题型进行判分。每道题作答完毕后立即显示结果和解析，并自动保存答题记录。')
    add_body_paragraph(doc, '（5）错题本需求：自动收集用户答错的题目，记录错误次数和掌握程度。支持查看全部错题、未掌握错题和错题统计摘要。答对错题时自动提升掌握程度。')
    add_body_paragraph(doc, '（6）每日计划需求：用户可以创建每日学习计划，设定目标题数。系统自动跟踪实际完成进度，以可视化进度条展示完成情况。支持查看最近N天的计划历史和连续打卡天数统计。')
    add_body_paragraph(doc, '（7）学习统计需求：提供综合统计面板，展示总体数据、今日数据、各科正确率、错题数据和打卡数据。支持将学习报告导出为CSV文件。')
    add_body_paragraph(doc, '（8）复习提醒需求：基于艾宾浩斯遗忘曲线，在用户答题后自动创建复习提醒。使用多线程后台定时任务（ScheduledExecutorService）每5分钟检查到期提醒，以守护线程方式运行，不影响主程序正常退出。')
    
    add_heading(doc, '2.1.2 系统开发环境概述', level=3)
    env_data = [
        ['开发语言', 'Java 17+'],
        ['开发工具', 'IntelliJ IDEA / VS Code'],
        ['数据库', 'SQLite 3（嵌入式）'],
        ['构建工具', 'javac 直接编译 / Maven（可选）'],
        ['运行环境', 'JDK 17+，支持 Windows/macOS/Linux'],
        ['设计模式', '工厂模式、策略模式、模板方法模式、单例模式'],
        ['交互方式', '控制台命令行界面（Console UI）'],
        ['字符编码', 'UTF-8'],
    ]
    add_table_with_data(doc, ['项目', '说明'], env_data)
    
    doc.add_page_break()
    
    # ========== CHAPTER 3: 系统详细需求分析 ==========
    add_heading(doc, '3 系统详细需求分析', level=1)
    add_body_paragraph(doc, '本章对MindFlow考研备考复习系统的各功能模块进行详细的需求分析，明确每个功能的输入、处理逻辑和输出要求。以下表格对各模块的功能需求进行了系统化的梳理和描述。')
    
    req_data = [
        ['用户管理', '用户注册', '用户名、密码、昵称', '校验输入有效性；检查用户名唯一性；密码SHA-256哈希加密；写入users表；初始化4个预设科目', '返回User对象；控制台提示注册成功'],
        ['用户管理', '用户登录', '用户名、密码', '查询users表；验证密码哈希匹配；设置当前会话用户', '登录成功返回User对象；失败抛出BusinessException'],
        ['用户管理', '修改密码', '原密码、新密码', '验证原密码正确性；校验新密码长度；更新users表password字段', '控制台提示修改成功'],
        ['用户管理', '更新个人信息', '昵称、目标院校、目标专业、考试日期', '获取当前用户；更新对应字段；写入users表', '控制台提示更新成功'],
        ['科目管理', '查看科目列表', '当前用户ID', '查询subjects表；统计每个科目的题目数量', '格式化显示科目列表含题目计数'],
        ['科目管理', '添加科目', '科目名、描述、总分', '获取当前用户ID；插入subjects表', '返回Subject对象'],
        ['科目管理', '删除科目', '科目ID', '检查是否为预设科目；检查关联题目数；确认后级联删除', '控制台提示删除成功或取消'],
        ['科目管理', '知识点管理', '科目ID、父节点ID、知识点名', '插入knowledge_points表；支持树形层级', '控制台提示添加成功'],
        ['题库管理', '添加题目', '科目ID、题型、难度、内容、选项/答案、解析', '通过QuestionFactory创建对应子类；插入questions表', '返回Question对象含自增ID'],
        ['题库管理', '查看题目列表', '科目ID', '查询questions表；格式化显示题型、难度、内容摘要', '控制台分页显示题目列表'],
        ['题库管理', '搜索题目', '科目ID、关键词', '模糊匹配content字段', '返回匹配的Question列表'],
        ['题库管理', '删除题目', '题目ID', '确认操作；删除questions表记录', '控制台提示删除成功'],
        ['答题练习', '随机练习', '科目ID、题目数量', '从questions表随机抽取指定数量题目', '进入逐题作答流程'],
        ['答题练习', '错题重做', '题目数量', '查询wrong_questions表中未掌握的题目', '进入错题重做流程'],
        ['答题练习', '判分处理', '题目对象、用户答案', '通过GradingStrategyFactory获取对应策略；执行grade方法', '返回布尔值表示是否正确'],
        ['答题练习', '答题记录', '用户ID、题目ID、答案、是否正确、用时', '插入answer_records表；更新错题本；创建复习提醒', '本轮结束显示统计摘要'],
        ['错题本', '查看错题列表', '当前用户ID', '联查wrong_questions和questions表', '格式化显示错题信息和掌握程度'],
        ['错题本', '错题统计', '当前用户ID', '统计总数、未掌握数、已掌握数', '显示统计摘要和掌握率百分比'],
        ['每日计划', '创建计划', '科目ID、目标题数、备注', '插入daily_plans表', '控制台提示创建成功'],
        ['每日计划', '查看今日计划', '当前用户ID', '查询当天计划；统计今日答题数和正确数', '显示进度条和完成率'],
        ['学习统计', '综合面板', '当前用户ID', '聚合answer_records、study_sessions、wrong_questions、daily_plans数据', 'ASCII图表显示各维度统计'],
        ['学习统计', '导出报告', '当前用户ID', '汇总各科统计数据；调用FileExportUtil导出CSV', '返回导出文件路径'],
        ['复习提醒', '定时检查', '用户ID', 'ScheduledExecutorService每5分钟触发；查询review_reminders表到期记录', '控制台输出待复习题目列表'],
        ['复习提醒', '创建提醒', '用户ID、题目ID', '根据艾宾浩斯曲线计算下次复习日期；插入review_reminders表', '自动创建下一轮复习计划'],
    ]
    add_table_with_data(doc, ['模块', '功能', '输入', '处理逻辑', '输出'], req_data)
    
    doc.add_page_break()
    
    # ========== CHAPTER 4: 总体方案确认 ==========
    add_heading(doc, '4 总体方案确认', level=1)
    
    add_heading(doc, '4.1 系统总体结构确认', level=2)
    add_body_paragraph(doc, 'MindFlow考研备考复习系统采用经典的分层架构设计，将整个系统划分为表现层、业务逻辑层、数据访问层和数据存储层四个层次。这种分层架构使得各层职责清晰、相互解耦，便于独立开发、测试和维护。')
    add_body_paragraph(doc, '表现层（Console UI）：由Main.java中的菜单驱动循环构成，负责接收用户输入、调用业务服务、格式化输出结果。该层不包含任何业务逻辑，仅作为用户与系统交互的入口。')
    add_body_paragraph(doc, '业务逻辑层（Service）：包含UserService、SubjectService、QuestionService、QuizService、WrongBookService、PlanService、StatisticsService共7个核心服务类，以及SampleDataService辅助服务。每个服务类采用单例模式（Singleton），通过getInstance()方法获取实例，确保全局状态一致性。业务逻辑层负责业务流程编排、数据校验、异常处理和跨模块协调。')
    add_body_paragraph(doc, '数据访问层（DAO）：包含UserDao、SubjectDao、QuestionDao、AnswerRecordDao、WrongQuestionDao、DailyPlanDao、StudySessionDao、KnowledgePointDao、ReviewReminderDao共9个DAO类，均继承自BaseDao基类。DAO层封装了所有SQL操作，对外提供面向对象的CRUD接口，屏蔽了底层数据库细节。')
    add_body_paragraph(doc, '数据存储层（SQLite）：使用SQLite嵌入式数据库，数据库文件存储在本地磁盘上，无需独立的数据库服务器进程。通过DbUtil工具类统一管理数据库连接的获取和释放。')
    
    add_heading(doc, '4.2 系统架构及模块设计', level=2)
    
    add_heading(doc, '4.2.1 系统架构', level=3)
    add_body_paragraph(doc, '系统整体架构遵循MVC（Model-View-Controller）设计思想的控制台变体。Model层由model包下的实体类组成，包括User、Subject、Question及其子类（ChoiceQuestion、TrueFalseQuestion、FillBlankQuestion）、AnswerRecord、WrongQuestion、DailyPlan、StudySession、KnowledgePoint、ReviewReminder等。View层和Controller层合并为Main.java中的控制台交互逻辑，通过调用Service层的方法来完成业务处理。')
    add_body_paragraph(doc, '系统还包含了util工具包（ConsoleUtil、DbUtil、FileExportUtil、PasswordUtil）、exception异常包（BusinessException、DataNotFoundException、DuplicateException）、factory工厂包（QuestionFactory）、strategy策略包（GradingStrategy接口及其三个实现类）和thread线程包（ReviewReminderThread）。这些辅助模块为核心业务提供了基础设施支撑。')
    
    add_heading(doc, '4.2.2 设计模式应用', level=3)
    add_body_paragraph(doc, 'MindFlow系统在多个关键场景中运用了经典的面向对象设计模式，以下逐一说明：')
    
    add_body_paragraph(doc, '（1）工厂模式（Factory Pattern）：QuestionFactory类作为题目创建的统一入口，提供了createChoice()、createTrueFalse()、createFillBlank()三个静态工厂方法，以及createFromResultSet()方法用于从数据库ResultSet构建多态对象。工厂模式将对象的创建逻辑集中管理，避免了在业务代码中直接使用new关键字创建具体子类，提高了代码的可扩展性——当需要新增题型时，只需在工厂类中添加对应的创建方法即可。', bold=False)
    
    add_body_paragraph(doc, '（2）策略模式（Strategy Pattern）：GradingStrategy接口定义了grade()、getCorrectAnswerDisplay()、getTypeCode()三个方法，分别对应判分逻辑、正确答案展示和题型标识。ChoiceGrading、TrueFalseGrading、FillBlankGrading三个实现类分别处理选择题的大小写不敏感比较、判断题的T/F匹配和填空题的多空JSON解析。GradingStrategyFactory根据题型代码返回对应的策略实例。策略模式使得判分逻辑与答题流程完全解耦，新增题型只需增加一个策略实现类。')
    
    add_body_paragraph(doc, '（3）模板方法模式（Template Method Pattern）：Question抽象基类定义了formatDisplay()方法作为题目展示的模板，其中包含题型标签、难度星级和内容显示的通用逻辑。ChoiceQuestion子类重写了该方法，在父类输出的基础上追加了A/B/C/D四个选项的显示。模板方法模式确保了所有题型的展示格式保持一致的基础框架，同时允许子类定制特定部分。')
    
    add_body_paragraph(doc, '（4）单例模式（Singleton Pattern）：所有Service类和DAO类均采用饿汉式单例模式，通过private static final INSTANCE字段和public static getInstance()方法实现。单例模式确保了全局只有一个服务实例，避免了重复创建带来的资源浪费和状态不一致问题，特别适合管理数据库连接和用户会话状态。')
    
    add_body_paragraph(doc, '（5）DAO模式（Data Access Object Pattern）：BaseDao抽象基类提供了通用的数据库操作方法（getConnection、closeResources等），各具体DAO类继承BaseDao并实现特定实体的CRUD操作。DAO模式将数据访问逻辑与业务逻辑分离，使得业务层不需要关心SQL语句的细节，同时也便于将来更换数据库时只修改DAO层而不影响上层代码。')
    
    doc.add_page_break()
    
    # ========== CHAPTER 5: 功能模块详细设计 ==========
    add_heading(doc, '5 功能模块详细设计', level=1)
    add_body_paragraph(doc, '本章对MindFlow考研备考复习系统的八个核心功能模块进行详细设计说明。每个模块按照"模块概述→功能点说明→核心代码展示"的结构组织，代码来源于项目实际源码文件，确保设计与实现的一致性。')
    
    # ----- 5.1 用户管理模块 -----
    add_heading(doc, '5.1 用户管理模块', level=2)
    add_body_paragraph(doc, '用户管理模块是系统的基础模块，负责用户的注册、登录、登出和个人信息管理等核心功能。该模块通过UserService业务服务类和UserDao数据访问类协同工作，实现了完整的用户生命周期管理。用户注册时系统会自动为新用户初始化政治、英语、数学、专业课四个考研预设科目，确保用户首次登录后即可开始使用系统。密码采用SHA-256哈希算法加密存储，确保用户凭证的安全性。该模块涉及的数据表为users表和subjects表。')
    add_body_paragraph(doc, '下面对其功能进行说明：')
    
    add_body_paragraph(doc, '1、用户注册功能：用户在控制台输入用户名、密码和昵称后，系统首先校验用户名长度不少于2个字符、密码长度不少于4个字符。然后查询users表检查用户名是否已被占用，若已存在则抛出DuplicateException异常。校验通过后，使用PasswordUtil.hashPassword()方法对密码进行SHA-256哈希处理，将用户信息插入users表并获取自增主键。最后调用initPresetSubjects()方法为新用户创建四个预设科目。整个注册流程涉及users表的INSERT操作和subjects表的批量INSERT操作。')
    
    add_body_paragraph(doc, '2、用户登录功能：用户输入用户名和密码后，系统通过UserDao.findByUsername()方法查询users表获取用户记录。若用户不存在则抛出"用户不存在"异常；若存在则使用PasswordUtil.verifyPassword()方法比对密码哈希值，不匹配则抛出"密码错误"异常。验证成功后将User对象赋值给currentUser字段，建立会话级别的用户上下文。后续所有需要身份验证的操作都通过getCurrentUser()方法获取当前登录用户。')
    
    add_body_paragraph(doc, '3、用户登出功能：将currentUser字段置为null，清除会话上下文。登出前会输出提示信息告知用户已成功退出。登出后再次调用需要登录的功能时会触发"请先登录"异常。')
    
    add_body_paragraph(doc, '4、修改密码功能：用户输入原密码和新密码后，系统先验证原密码的正确性，再校验新密码的长度要求。验证通过后使用PasswordUtil.hashPassword()对新密码进行哈希处理，调用UserDao.updatePassword()方法更新users表中的password字段。')
    
    add_body_paragraph(doc, '5、更新个人信息功能：用户可以修改昵称、目标院校、目标专业和考试日期四项个人信息。系统获取当前登录用户对象，更新对应字段后调用UserDao.update()方法将变更写入users表。其中昵称为可选更新项，传入null或空字符串时保持原值不变。')
    
    add_body_paragraph(doc, '核心代码展示：')
    add_body_paragraph(doc, '业务逻辑层（UserService.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('service/UserService.java'))
    
    doc.add_page_break()
    
    # ----- 5.2 科目管理模块 -----
    add_heading(doc, '5.2 科目管理模块', level=2)
    add_body_paragraph(doc, '科目管理模块负责考研科目的增删改查以及知识点管理功能。该模块通过SubjectService业务服务类和SubjectDao、KnowledgePointDao数据访问类协同工作。系统预设了政治、英语、数学、专业课四个标准考研科目，这些预设科目不允许删除但可以修改描述和总分。用户还可以根据自身情况添加自定义科目。每个科目下可以建立树形结构的知识点体系，便于将题目按知识维度进行分类管理。该模块涉及的数据表为subjects表和knowledge_points表。')
    add_body_paragraph(doc, '下面对其功能进行说明：')
    
    add_body_paragraph(doc, '1、查看科目列表功能：通过SubjectDao.findByUserId()方法查询当前用户的所有科目，然后对每个科目调用QuestionDao.countBySubject()统计题目数量。输出时区分预设科目和自定义科目（预设科目带"[预设]"标记），并显示科目名称、总分和题目数量三项信息。')
    
    add_body_paragraph(doc, '2、添加自定义科目功能：获取当前用户ID，创建Subject对象（preset默认为false），调用SubjectDao.insert()方法写入subjects表。添加成功后控制台输出确认信息并返回带有自增ID的Subject对象。')
    
    add_body_paragraph(doc, '3、修改科目信息功能：对于预设科目，只允许修改描述和总分两个字段，名称保持不变；对于自定义科目，名称、描述和总分均可修改。修改后调用SubjectDao.update()方法更新subjects表对应记录。')
    
    add_body_paragraph(doc, '4、删除科目功能：首先检查目标科目是否为预设科目，若是则直接拒绝删除并抛出异常。对于自定义科目，先统计其下的题目数量，若有题目则提示用户确认是否级联删除。用户确认后调用SubjectDao.delete()方法删除subjects表记录（关联的题目和知识点通过外键级联删除）。')
    
    add_body_paragraph(doc, '5、选择科目功能：列出当前用户所有科目及其题目数量，让用户通过数字编号选择一个科目。该方法被答题练习、题库管理等其他模块调用，作为科目前置选择的统一入口。')
    
    add_body_paragraph(doc, '6、知识点管理功能：支持向指定科目添加知识点，知识点可以有父节点（parentId），形成树形层级结构。通过KnowledgePointDao.insert()方法写入knowledge_points表。displayKnowledgePoints()方法以缩进形式展示知识点的层级关系。')
    
    add_body_paragraph(doc, '核心代码展示：')
    add_body_paragraph(doc, '业务逻辑层（SubjectService.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('service/SubjectService.java'))
    
    doc.add_page_break()
    
    # ----- 5.3 题库管理模块 -----
    add_heading(doc, '5.3 题库管理模块', level=2)
    add_body_paragraph(doc, '题库管理模块是系统的核心数据管理模块，负责选择题、判断题、填空题三种题型的完整CRUD操作。该模块通过QuestionService业务服务类、QuestionFactory工厂类和QuestionDao数据访问类协同工作。QuestionFactory运用工厂模式统一创建不同类型的题目对象，并通过createFromResultSet()方法实现从数据库结果集到多态对象的映射。每道题目包含所属科目、关联知识点、难度等级（1-5星）、题目内容、答案和解析等信息。该模块涉及的数据表为questions表。')
    add_body_paragraph(doc, '下面对其功能进行说明：')
    
    add_body_paragraph(doc, '1、添加选择题功能：接收科目ID、知识点ID、难度、题目内容、四个选项（A/B/C/D）、正确答案和解析参数。通过QuestionFactory.createChoice()创建ChoiceQuestion对象，设置知识点ID和解析后调用QuestionDao.insert()写入questions表。数据库中以question_type="CHOICE"标识，option_a/b/c/d字段存储四个选项内容。')
    
    add_body_paragraph(doc, '2、添加判断题功能：接收科目ID、知识点ID、难度、题目内容、答案（T/F）和解析参数。通过QuestionFactory.createTrueFalse()创建TrueFalseQuestion对象。数据库中question_type="TRUE_FALSE"，answer字段存储"T"或"F"。')
    
    add_body_paragraph(doc, '3、添加填空题功能：接收科目ID、知识点ID、难度、题目内容、答案和解析参数。填空题的答案支持两种格式：单个空直接填写答案文本，多个空使用JSON格式{"1":"答案1","2":"答案2"}。数据库中question_type="FILL_BLANK"。')
    
    add_body_paragraph(doc, '4、交互式添加题目功能：提供控制台引导式的题目添加流程。首先让用户选择题型，然后依次输入题目内容、难度、是否关联知识点、解析等信息。对于选择题还需输入四个选项和正确答案，判断题输入T/F答案，填空题输入答案文本。整个过程有完善的输入校验和错误提示。')
    
    add_body_paragraph(doc, '5、查看题目列表功能：通过QuestionDao.findBySubjectId()获取指定科目的所有题目，以编号列表形式展示每道题的题型、难度星级和内容摘要（截取前60个字符）。')
    
    add_body_paragraph(doc, '6、搜索题目功能：通过QuestionDao.search()方法在指定科目范围内按关键词模糊搜索题目内容，返回匹配的Question列表。')
    
    add_body_paragraph(doc, '7、删除题目功能：先通过QuestionDao.findById()确认题目存在，然后提示用户确认操作，确认后调用QuestionDao.delete()删除questions表记录。')
    
    add_body_paragraph(doc, '核心代码展示：')
    add_body_paragraph(doc, '业务逻辑层（QuestionService.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('service/QuestionService.java'))
    add_body_paragraph(doc, '工厂类（QuestionFactory.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('factory/QuestionFactory.java'))
    
    doc.add_page_break()
    
    # ----- 5.4 答题练习模块 -----
    add_heading(doc, '5.4 答题练习模块', level=2)
    add_body_paragraph(doc, '答题练习模块是系统的核心业务模块，提供随机练习、错题重做和按题型练习三种练习模式。该模块通过QuizService业务服务类协调QuestionDao、AnswerRecordDao、WrongQuestionDao、ReviewReminderDao等多个数据访问类完成完整的答题流程。判分逻辑采用策略模式，通过GradingStrategyFactory根据题型动态获取对应的GradingStrategy实现类。每道题作答完毕后系统自动保存答题记录到answer_records表、更新错题本状态、创建艾宾浩斯复习提醒，并实时更新当日计划进度。该模块涉及的数据表包括questions表、answer_records表、wrong_questions表、review_reminders表和daily_plans表。')
    add_body_paragraph(doc, '下面对其功能进行说明：')
    
    add_body_paragraph(doc, '1、随机练习功能：用户指定科目和题目数量后，通过QuestionDao.findRandom()方法从questions表中随机抽取指定数量的题目。进入逐题作答循环，每道题显示题目内容后等待用户输入答案，同时使用System.currentTimeMillis()记录答题用时。')
    
    add_body_paragraph(doc, '2、错题重做功能：通过WrongQuestionDao.findUnresolved()获取当前用户所有未掌握的错题，取前N条对应的题目进入作答流程。若没有未掌握的错题则提示"太棒了！没有未掌握的错题"。')
    
    add_body_paragraph(doc, '3、按题型练习功能：让用户选择题型（选择题/判断题/填空题），通过QuestionDao.findByType()获取指定科目下该题型的所有题目，截取前N条进入作答流程。')
    
    add_body_paragraph(doc, '4、策略模式判分功能：每道题作答后，通过GradingStrategyFactory.getStrategy(typeCode)获取对应的判分策略。选择题使用ChoiceGrading进行大小写不敏感的字母比较；判断题使用TrueFalseGrading进行T/F匹配；填空题使用FillBlankGrading进行文本比对（支持多空JSON解析）。判分结果以布尔值返回。')
    
    add_body_paragraph(doc, '5、答题记录保存功能：每道题作答完毕后创建AnswerRecord对象（包含用户ID、题目ID、用户答案、是否正确、用时毫秒数），调用AnswerRecordDao.insert()写入answer_records表。这些记录是学习统计和错题追踪的数据基础。')
    
    add_body_paragraph(doc, '6、错题本联动功能：答对时调用WrongQuestionDao.improveMastery()提升该题的掌握程度；答错时调用WrongQuestionDao.addOrUpdate()增加错误次数或新增错题记录。这确保了错题本的实时更新。')
    
    add_body_paragraph(doc, '7、复习提醒创建功能：每道题作答后调用createReviewReminder()方法，以明天为首次复习日期、第1轮复习阶段创建ReviewReminder记录写入review_reminders表。后续复习完成后会自动推进到下一轮。')
    
    add_body_paragraph(doc, '8、本轮统计展示功能：所有题目作答完毕后，汇总显示总题数、正确数、正确率、总用时和平均每题用时五项统计数据。同时调用updateDailyPlanProgress()更新当日计划的实际完成数。')
    
    add_body_paragraph(doc, '核心代码展示：')
    add_body_paragraph(doc, '业务逻辑层（QuizService.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('service/QuizService.java'))
    add_body_paragraph(doc, '策略接口（GradingStrategy.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('strategy/GradingStrategy.java'))
    add_body_paragraph(doc, '选择题判分策略（ChoiceGrading.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('strategy/ChoiceGrading.java'))
    
    doc.add_page_break()
    
    # ----- 5.5 错题本模块 -----
    add_heading(doc, '5.5 错题本模块', level=2)
    add_body_paragraph(doc, '错题本模块负责自动收集和管理用户答错的题目，是系统智能复习体系的重要组成部分。该模块通过WrongBookService业务服务类和WrongQuestionDao数据访问类协同工作。每当用户在答题练习中答错一道题，系统会自动将该题加入错题本或增加错误次数；答对错题时则提升掌握程度。掌握程度分为四个等级：未掌握(0)、部分掌握(1)、基本掌握(2)、已掌握(3)，直观反映用户对每道错题的熟悉程度。该模块涉及的数据表为wrong_questions表和questions表（联查获取题目详情）。')
    add_body_paragraph(doc, '下面对其功能进行说明：')
    
    add_body_paragraph(doc, '1、查看全部错题功能：通过WrongQuestionDao.findByUserId()获取当前用户的所有错题记录，然后对每条记录调用QuestionDao.findById()获取题目详情。输出时显示序号、题型、错误次数、掌握程度标识（❌/🔶/🔷/✅）和题目内容摘要（截取前50个字符）。若错题本为空则显示鼓励信息。')
    
    add_body_paragraph(doc, '2、查看未掌握错题功能：通过WrongQuestionDao.findUnresolved()获取掌握程度低于3的错题。首先输出错题总数、未掌握数和已掌握数的概览信息，然后逐条列出未掌握错题的详细信息。若所有错题都已掌握则显示祝贺信息。')
    
    add_body_paragraph(doc, '3、错题统计摘要功能：分别统计当前用户的错题总数、未掌握数和已掌握数，计算并显示掌握率百分比。该功能为用户提供了错题管理的宏观视角，帮助用户评估自己的薄弱环节改善情况。')
    
    add_body_paragraph(doc, '4、错题本自动更新机制：该机制嵌入在QuizService的答题流程中。答错时调用WrongQuestionDao.addOrUpdate()，若该题已在错题本中则wrong_count加1且mastery_level降一级（最低为0），若不在则新增记录（wrong_count=1, mastery_level=0）。答对时调用WrongQuestionDao.improveMastery()，mastery_level升一级（最高为3），当mastery_level达到3时记录resolvedAt时间戳。')
    
    add_body_paragraph(doc, '核心代码展示：')
    add_body_paragraph(doc, '业务逻辑层（WrongBookService.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('service/WrongBookService.java'))
    
    doc.add_page_break()
    
    # ----- 5.6 每日计划模块 -----
    add_heading(doc, '5.6 每日计划模块', level=2)
    add_body_paragraph(doc, '每日计划模块为用户提供学习目标制定和进度追踪功能，帮助考生养成规律学习的习惯。该模块通过PlanService业务服务类和DailyPlanDao、AnswerRecordDao数据访问类协同工作。用户可以每天创建学习计划，设定目标题数和备注说明。系统自动统计当日实际答题数量和正确数量，以可视化进度条展示完成进度。此外还支持查看最近N天的计划历史和连续打卡天数统计，激励用户保持学习连续性。该模块涉及的数据表为daily_plans表和answer_records表。')
    add_body_paragraph(doc, '下面对其功能进行说明：')
    
    add_body_paragraph(doc, '1、创建今日计划功能：获取当前用户ID和当天日期，创建DailyPlan对象（包含userId、planDate、targetCount），可选设置subjectId和note字段。调用DailyPlanDao.insert()写入daily_plans表。创建成功后控制台提示目标题数。')
    
    add_body_paragraph(doc, '2、查看今日计划功能：通过DailyPlanDao.findToday()获取当天的所有计划记录。同时调用AnswerRecordDao.countToday()和countCorrectToday()获取今日实际答题数和正确数。以ASCII进度条（█░）可视化展示完成百分比，当实际完成数达到或超过目标时显示庆祝信息。底部还显示今日答题总数、正确数和正确率，以及近30天完成天数统计。')
    
    add_body_paragraph(doc, '3、查看近期计划功能：通过DailyPlanDao.findRecent()获取最近N天的计划记录，以列表形式展示每天的日期、目标题数、完成题数和进度条。已完成的天用✅标记，未完成用⬜标记，一目了然。')
    
    add_body_paragraph(doc, '4、快速打卡功能：提供简化的计划创建流程，用户只需输入目标题数和可选备注即可快速创建今日计划，适合日常高频使用场景。')
    
    add_body_paragraph(doc, '5、进度自动更新机制：在QuizService的答题流程结束后，调用updateDailyPlanProgress()方法遍历当天的所有计划记录，将本次答题数量累加到actualCount字段。这确保了计划进度与实际答题行为的实时同步。')
    
    add_body_paragraph(doc, '核心代码展示：')
    add_body_paragraph(doc, '业务逻辑层（PlanService.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('service/PlanService.java'))
    
    doc.add_page_break()
    
    # ----- 5.7 学习统计模块 -----
    add_heading(doc, '5.7 学习统计模块', level=2)
    add_body_paragraph(doc, '学习统计模块为用户提供全方位的学习数据分析功能，帮助考生量化学习效果、发现薄弱环节。该模块通过StatisticsService业务服务类聚合AnswerRecordDao、StudySessionDao、WrongQuestionDao、DailyPlanDao等多个数据源的信息，并以ASCII图表的形式在控制台呈现。此外还支持将各科统计数据导出为CSV文件，方便用户在Excel等工具中进行更深入的分析。该模块涉及的数据表包括answer_records表、study_sessions表、wrong_questions表、daily_plans表和subjects表。')
    add_body_paragraph(doc, '下面对其功能进行说明：')
    
    add_body_paragraph(doc, '1、综合统计面板功能：showDashboard()方法是统计模块的核心入口，它依次展示五个维度的统计数据。总体数据维度：统计用户的总答题数、总正确数和总正确率。今日数据维度：统计当天的答题数、正确数和学习时长（从study_sessions表汇总durationMs转换为分钟）。各科正确率维度：遍历用户所有科目，分别计算每个科目的答题数和正确率，以ASCII柱状图（█░）直观对比各科表现。错题数据维度：显示错题总数和未掌握数。打卡数据维度：显示近30天的计划完成天数。')
    
    add_body_paragraph(doc, '2、导出学习报告功能：exportReport()方法汇总各科的总题数、正确数和正确率数据，构建表头和数据行列表，调用FileExportUtil.exportCsv()方法生成带BOM头的UTF-8编码CSV文件。文件名包含"学习统计"前缀和时间戳后缀，保存在exports目录下。CSV字段自动处理逗号和引号的转义，确保Excel打开时的兼容性。')
    
    add_body_paragraph(doc, '3、进度条渲染功能：renderBar()私有方法根据百分比值和宽度参数生成ASCII进度条字符串。filled部分用█字符填充，empty部分用░字符填充，直观展示比例关系。该方法被综合面板中的各科正确率展示所调用。')
    
    add_body_paragraph(doc, '核心代码展示：')
    add_body_paragraph(doc, '业务逻辑层（StatisticsService.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('service/StatisticsService.java'))
    add_body_paragraph(doc, '文件导出工具（FileExportUtil.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('util/FileExportUtil.java'))
    
    doc.add_page_break()
    
    # ----- 5.8 复习提醒模块 -----
    add_heading(doc, '5.8 复习提醒模块', level=2)
    add_body_paragraph(doc, '复习提醒模块基于艾宾浩斯遗忘曲线理论，为用户提供科学的复习节奏指导。该模块通过ReviewReminderThread多线程类和ReviewReminderDao数据访问类协同工作。艾宾浩斯遗忘曲线定义了7个复习周期：1天、2天、4天、7天、15天、30天、90天，系统在每个周期到期时自动提醒用户复习对应题目。提醒功能使用ScheduledExecutorService以守护线程方式每5分钟检查一次到期的复习任务，不阻塞主线程也不阻止程序正常退出。该模块涉及的数据表为review_reminders表。')
    add_body_paragraph(doc, '下面对其功能进行说明：')
    
    add_body_paragraph(doc, '1、定时检查功能：startScheduled()方法创建一个单线程的ScheduledExecutorService，以5分钟为固定间隔调度checkAndNotify()方法。线程设置为daemon守护线程，确保当主程序退出时后台线程自动终止，不会导致JVM挂起。线程命名为"ReviewReminder-Thread"便于调试识别。')
    
    add_body_paragraph(doc, '2、到期提醒通知功能：checkAndNotify()方法通过ReviewReminderDao.findDueToday()查询当前用户当天到期的复习提醒记录。若有到期记录则在控制台输出警告级别的提醒信息，逐条列出题目ID、当前复习轮次和对应的间隔天数。该方法内部捕获所有异常，防止单次查询失败导致定时任务中断。')
    
    add_body_paragraph(doc, '3、停止定时检查功能：stop()方法先将running标志置为false阻止后续检查，然后优雅关闭ScheduledExecutorService。先尝试awaitTermination等待3秒让正在执行的任务完成，超时则强制shutdownNow()。若等待过程中被中断也执行强制关闭并恢复中断标志。')
    
    add_body_paragraph(doc, '4、手动触发检查功能：manualCheck()方法允许用户在不等待定时周期的情况下主动触发一次到期检查，适用于用户刚启动系统时希望立即获知待复习任务的场景。')
    
    add_body_paragraph(doc, '5、复习提醒创建机制：在QuizService的答题流程中，每道题作答完毕后调用createReviewReminder()方法。该方法以明天为首次复习日期、第1轮复习阶段创建ReviewReminder记录。ReviewReminder类的getIntervalDays()静态方法定义了7个阶段对应的天数间隔，供提醒展示和后续轮次计算使用。MAX_STAGE常量定义为7，表示完成第7轮复习后不再创建新的提醒。')
    
    add_body_paragraph(doc, '核心代码展示：')
    add_body_paragraph(doc, '复习提醒线程（ReviewReminderThread.java）：', bold=True, first_line_indent=False)
    add_code_block(doc, read_source('thread/ReviewReminderThread.java'))
    
    doc.add_page_break()
    
    # ========== CHAPTER 6: 数据库设计 ==========
    add_heading(doc, '6 数据库设计', level=1)
    
    add_heading(doc, '6.1 概念设计', level=2)
    add_body_paragraph(doc, 'MindFlow考研备考复习系统的数据库采用SQLite嵌入式关系数据库，共设计了9张核心数据表。系统实体关系模型以用户(User)为核心实体，一个用户可以拥有多个科目(Subject)，每个科目下可以有多个知识点(KnowledgePoint)和多道题目(Question)。用户的每次答题行为产生答题记录(AnswerRecord)，答错的题目进入错题本(WrongQuestion)。用户每天可以制定学习计划(DailyPlan)，每次连续学习产生学习会话(StudySession)，系统还会为用户创建复习提醒(ReviewReminder)。')
    add_body_paragraph(doc, '各实体之间的主要关系如下：User与Subject为一对多关系（user_id外键）；Subject与Question为一对多关系（subject_id外键）；Subject与KnowledgePoint为一对多关系（subject_id外键）；KnowledgePoint支持自引用实现树形结构（parent_id外键指向自身表的kp_id）；Question与KnowledgePoint为多对一关系（kp_id外键，可为NULL）；User与AnswerRecord为一对多关系；User与WrongQuestion为一对多关系；User与DailyPlan为一对多关系；User与StudySession为一对多关系；User与ReviewReminder为一对多关系；Question与AnswerRecord为一对多关系；Question与WrongQuestion为一对多关系；Question与ReviewReminder为一对多关系。')
    
    add_heading(doc, '6.2 表设计', level=2)
    add_body_paragraph(doc, '以下对各数据表的字段结构进行详细说明。每张表均包含必要的约束条件以保证数据的完整性和一致性。')
    
    # users表
    p0 = doc.add_paragraph(style='p0')
    run = p0.add_run('users表')
    set_run_font(run, '宋体', Pt(12), bold=True)
    
    users_rows = [
        ['user_id', 'INTEGER', 'PRIMARY KEY AUTOINCREMENT', '用户唯一标识'],
        ['username', 'VARCHAR(50)', 'NOT NULL UNIQUE', '登录用户名'],
        ['password', 'VARCHAR(128)', 'NOT NULL', 'SHA-256哈希后的密码'],
        ['nickname', 'VARCHAR(50)', 'NOT NULL', '用户昵称'],
        ['target_school', 'VARCHAR(100)', 'NULL', '目标报考院校'],
        ['target_major', 'VARCHAR(100)', 'NULL', '目标报考专业'],
        ['exam_date', 'VARCHAR(20)', 'NULL', '考试日期(yyyy-MM-dd)'],
        ['created_at', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '注册时间'],
        ['updated_at', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '最后更新时间'],
    ]
    add_table_with_data(doc, ['字段名', '数据类型', '约束', '说明'], users_rows)
    doc.add_paragraph()  # spacing
    
    # subjects表
    p0 = doc.add_paragraph(style='p0')
    run = p0.add_run('subjects表')
    set_run_font(run, '宋体', Pt(12), bold=True)
    
    subjects_rows = [
        ['subject_id', 'INTEGER', 'PRIMARY KEY AUTOINCREMENT', '科目唯一标识'],
        ['user_id', 'INTEGER', 'NOT NULL, FK→users.user_id', '所属用户'],
        ['name', 'VARCHAR(50)', 'NOT NULL', '科目名称'],
        ['description', 'VARCHAR(200)', 'NULL', '科目描述'],
        ['total_score', 'REAL', 'NOT NULL DEFAULT 100', '科目满分分值'],
        ['is_preset', 'BOOLEAN', 'NOT NULL DEFAULT 0', '是否为预设科目(0/1)'],
        ['created_at', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '创建时间'],
    ]
    add_table_with_data(doc, ['字段名', '数据类型', '约束', '说明'], subjects_rows)
    doc.add_paragraph()
    
    # knowledge_points表
    p0 = doc.add_paragraph(style='p0')
    run = p0.add_run('knowledge_points表')
    set_run_font(run, '宋体', Pt(12), bold=True)
    
    kp_rows = [
        ['kp_id', 'INTEGER', 'PRIMARY KEY AUTOINCREMENT', '知识点唯一标识'],
        ['subject_id', 'INTEGER', 'NOT NULL, FK→subjects.subject_id', '所属科目'],
        ['parent_id', 'INTEGER', 'NULL, FK→knowledge_points.kp_id', '父知识点ID(NULL表示根节点)'],
        ['name', 'VARCHAR(100)', 'NOT NULL', '知识点名称'],
        ['sort_order', 'INTEGER', 'NOT NULL DEFAULT 0', '排序序号'],
    ]
    add_table_with_data(doc, ['字段名', '数据类型', '约束', '说明'], kp_rows)
    doc.add_paragraph()
    
    # questions表
    p0 = doc.add_paragraph(style='p0')
    run = p0.add_run('questions表')
    set_run_font(run, '宋体', Pt(12), bold=True)
    
    questions_rows = [
        ['question_id', 'INTEGER', 'PRIMARY KEY AUTOINCREMENT', '题目唯一标识'],
        ['subject_id', 'INTEGER', 'NOT NULL, FK→subjects.subject_id', '所属科目'],
        ['kp_id', 'INTEGER', 'NULL, FK→knowledge_points.kp_id', '关联知识点'],
        ['question_type', 'VARCHAR(20)', 'NOT NULL', '题型(CHOICE/TRUE_FALSE/FILL_BLANK)'],
        ['difficulty', 'INTEGER', 'NOT NULL CHECK(1-5)', '难度等级(1-5星)'],
        ['content', 'TEXT', 'NOT NULL', '题目内容'],
        ['option_a', 'VARCHAR(500)', 'NULL', '选项A(仅选择题)'],
        ['option_b', 'VARCHAR(500)', 'NULL', '选项B(仅选择题)'],
        ['option_c', 'VARCHAR(500)', 'NULL', '选项C(仅选择题)'],
        ['option_d', 'VARCHAR(500)', 'NULL', '选项D(仅选择题)'],
        ['answer', 'TEXT', 'NOT NULL', '正确答案'],
        ['explanation', 'TEXT', 'NULL', '题目解析'],
        ['created_at', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '创建时间'],
    ]
    add_table_with_data(doc, ['字段名', '数据类型', '约束', '说明'], questions_rows)
    doc.add_paragraph()
    
    # answer_records表
    p0 = doc.add_paragraph(style='p0')
    run = p0.add_run('answer_records表')
    set_run_font(run, '宋体', Pt(12), bold=True)
    
    ar_rows = [
        ['record_id', 'INTEGER', 'PRIMARY KEY AUTOINCREMENT', '记录唯一标识'],
        ['user_id', 'INTEGER', 'NOT NULL, FK→users.user_id', '答题用户'],
        ['question_id', 'INTEGER', 'NOT NULL, FK→questions.question_id', '所答题目'],
        ['user_answer', 'TEXT', 'NOT NULL', '用户提交的答案'],
        ['is_correct', 'BOOLEAN', 'NOT NULL', '是否正确(0/1)'],
        ['time_spent_ms', 'BIGINT', 'NOT NULL', '答题耗时(毫秒)'],
        ['answered_at', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '答题时间'],
    ]
    add_table_with_data(doc, ['字段名', '数据类型', '约束', '说明'], ar_rows)
    doc.add_paragraph()
    
    # wrong_questions表
    p0 = doc.add_paragraph(style='p0')
    run = p0.add_run('wrong_questions表')
    set_run_font(run, '宋体', Pt(12), bold=True)
    
    wq_rows = [
        ['wq_id', 'INTEGER', 'PRIMARY KEY AUTOINCREMENT', '错题记录唯一标识'],
        ['user_id', 'INTEGER', 'NOT NULL, FK→users.user_id', '所属用户'],
        ['question_id', 'INTEGER', 'NOT NULL, FK→questions.question_id', '错题题目'],
        ['wrong_count', 'INTEGER', 'NOT NULL DEFAULT 1', '累计错误次数'],
        ['mastery_level', 'INTEGER', 'NOT NULL DEFAULT 0', '掌握程度(0-3)'],
        ['last_wrong_at', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '最近一次答错时间'],
        ['resolved_at', 'DATETIME', 'NULL', '掌握时间(mastery_level=3时记录)'],
    ]
    add_table_with_data(doc, ['字段名', '数据类型', '约束', '说明'], wq_rows)
    doc.add_paragraph()
    
    # daily_plans表
    p0 = doc.add_paragraph(style='p0')
    run = p0.add_run('daily_plans表')
    set_run_font(run, '宋体', Pt(12), bold=True)
    
    dp_rows = [
        ['plan_id', 'INTEGER', 'PRIMARY KEY AUTOINCREMENT', '计划唯一标识'],
        ['user_id', 'INTEGER', 'NOT NULL, FK→users.user_id', '所属用户'],
        ['plan_date', 'DATE', 'NOT NULL', '计划日期'],
        ['subject_id', 'INTEGER', 'NULL, FK→subjects.subject_id', '关联科目(NULL表示全科)'],
        ['target_count', 'INTEGER', 'NOT NULL', '目标答题数量'],
        ['actual_count', 'INTEGER', 'NOT NULL DEFAULT 0', '实际完成数量'],
        ['is_completed', 'BOOLEAN', 'NOT NULL DEFAULT 0', '是否完成(0/1)'],
        ['study_minutes', 'INTEGER', 'DEFAULT 0', '学习时长(分钟)'],
        ['note', 'VARCHAR(200)', 'NULL', '备注说明'],
        ['created_at', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '创建时间'],
    ]
    add_table_with_data(doc, ['字段名', '数据类型', '约束', '说明'], dp_rows)
    doc.add_paragraph()
    
    # study_sessions表
    p0 = doc.add_paragraph(style='p0')
    run = p0.add_run('study_sessions表')
    set_run_font(run, '宋体', Pt(12), bold=True)
    
    ss_rows = [
        ['session_id', 'INTEGER', 'PRIMARY KEY AUTOINCREMENT', '会话唯一标识'],
        ['user_id', 'INTEGER', 'NOT NULL, FK→users.user_id', '所属用户'],
        ['subject_id', 'INTEGER', 'NULL, FK→subjects.subject_id', '学习科目'],
        ['start_time', 'DATETIME', 'NOT NULL', '开始时间'],
        ['end_time', 'DATETIME', 'NULL', '结束时间'],
        ['duration_ms', 'BIGINT', 'DEFAULT 0', '持续时长(毫秒)'],
        ['questions_done', 'INTEGER', 'DEFAULT 0', '完成题目数'],
    ]
    add_table_with_data(doc, ['字段名', '数据类型', '约束', '说明'], ss_rows)
    doc.add_paragraph()
    
    # review_reminders表
    p0 = doc.add_paragraph(style='p0')
    run = p0.add_run('review_reminders表')
    set_run_font(run, '宋体', Pt(12), bold=True)
    
    rr_rows = [
        ['reminder_id', 'INTEGER', 'PRIMARY KEY AUTOINCREMENT', '提醒唯一标识'],
        ['user_id', 'INTEGER', 'NOT NULL, FK→users.user_id', '所属用户'],
        ['question_id', 'INTEGER', 'NOT NULL, FK→questions.question_id', '待复习题目'],
        ['review_date', 'VARCHAR(10)', 'NOT NULL', '计划复习日期(yyyy-MM-dd)'],
        ['review_stage', 'INTEGER', 'NOT NULL CHECK(1-7)', '复习阶段(1-7轮)'],
        ['is_reviewed', 'BOOLEAN', 'NOT NULL DEFAULT 0', '是否已完成复习(0/1)'],
    ]
    add_table_with_data(doc, ['字段名', '数据类型', '约束', '说明'], rr_rows)
    
    doc.add_page_break()
    
    # ========== CHAPTER 7: 系统安全设计 ==========
    add_heading(doc, '7 系统安全设计', level=1)
    
    add_heading(doc, '7.1 安全设计说明', level=2)
    add_body_paragraph(doc, 'MindFlow考研备考复习系统虽然是一个控制台应用程序，但在安全设计方面仍然遵循了基本的安全原则，确保用户数据的安全性和系统的稳定运行。由于系统采用本地部署、嵌入式数据库的架构，不涉及网络通信和Web服务，因此安全设计的重点集中在数据存储安全和访问控制两个方面。')
    add_body_paragraph(doc, '系统的安全威胁模型主要包括：未授权访问（他人使用同一设备访问其他用户数据）、密码泄露（明文存储或弱哈希导致密码被破解）、数据完整性破坏（恶意或意外修改数据库文件）以及注入攻击（SQL注入导致数据泄露或篡改）。针对这些威胁，系统在设计和实现层面采取了相应的防护措施。')
    
    add_heading(doc, '7.2 安全设计方案', level=2)
    
    add_body_paragraph(doc, '（1）密码安全：系统使用PasswordUtil工具类对用户密码进行SHA-256哈希处理后存储，数据库中不保存任何明文密码。登录验证时使用相同的哈希算法对用户输入的密码进行处理后与数据库中的哈希值进行比较。SHA-256是目前广泛使用的密码学安全哈希函数，具有抗碰撞和单向性特点，即使数据库文件被获取也无法直接还原用户密码。')
    
    add_body_paragraph(doc, '（2）会话管理：系统采用内存级会话管理机制，currentUser字段保存在UserService的单例实例中。用户登出时立即将该字段置为null，清除会话上下文。所有需要身份验证的业务方法在执行前都会调用getCurrentUser()方法检查登录状态，未登录时抛出BusinessException异常阻止操作。这种设计确保了只有经过认证的用户才能访问系统功能。')
    
    add_body_paragraph(doc, '（3）输入校验：系统在多个入口处实施了严格的输入校验。用户注册时校验用户名和密码的最小长度；添加题目时校验难度范围（1-5）和答案格式；修改密码时校验新密码长度。所有校验失败均通过BusinessException异常反馈给用户，避免了非法数据进入系统。DAO层的SQL语句全部使用PreparedStatement参数化查询，从根本上防止了SQL注入攻击。')
    
    add_body_paragraph(doc, '（4）数据隔离：每个用户的数据通过user_id外键进行隔离。所有查询操作都会在WHERE子句中加入user_id条件，确保用户只能访问自己的数据。例如SubjectDao.findByUserId()、WrongQuestionDao.findByUserId()等方法都以userId作为必传参数。这种数据隔离机制即使在单用户控制台环境下也为未来可能的多用户扩展奠定了基础。')
    
    add_body_paragraph(doc, '（5）数据库文件安全：SQLite数据库文件存储在应用程序本地目录中，受操作系统文件权限保护。建议在生产部署时将数据库文件所在目录的权限设置为仅应用程序所有者可读写（chmod 600），防止其他系统用户直接读取数据库文件。同时，系统的关键操作（如删除科目、删除题目）都设置了二次确认机制，防止误操作导致数据丢失。')
    
    add_body_paragraph(doc, '（6）异常处理安全：系统定义了BusinessException、DataNotFoundException、DuplicateException三种业务异常类，所有异常信息都经过精心设计，不泄露系统内部实现细节（如SQL语句、表结构、堆栈信息等）。控制台输出的错误消息仅包含用户可理解的业务层面描述，避免了信息泄露风险。')
    
    add_body_paragraph(doc, '（7）线程安全：ReviewReminderThread使用了volatile关键字修饰running标志变量，确保多线程环境下的可见性。ScheduledExecutorService本身是线程安全的，checkAndNotify()方法内部的数据库操作通过独立的Connection实例执行，避免了共享连接的并发问题。守护线程的设置确保了后台任务不会阻止JVM正常退出，避免了因线程泄漏导致的资源安全问题。')
    
    # ========== SAVE ==========
    print("Saving document...")
    doc.save(OUTPUT_PATH)
    print(f"Document saved to: {OUTPUT_PATH}")
    
    # Verify
    print("\nVerifying...")
    vdoc = Document(OUTPUT_PATH)
    para_count = len(vdoc.paragraphs)
    table_count = len(vdoc.tables)
    heading_count = sum(1 for p in vdoc.paragraphs if p.style.name.startswith('Heading'))
    print(f"Paragraphs: {para_count}")
    print(f"Tables: {table_count}")
    print(f"Headings: {heading_count}")


if __name__ == '__main__':
    main()
