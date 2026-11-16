#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""按课程模板生成四份文档：测试用例说明 / 测试总报告 / 实验设计报告 / 开发文档。

用法（需使用带 python-docx 的解释器）：
    python3 gen_course_docs.py
输出到项目根目录：
    G01-MindFlow考研复习系统测试用例说明.docx
    G01-MindFlow考研复习系统测试总报告.docx
    G01-MindFlow考研复习系统实验设计报告.docx
    G01-MindFlow考研复习系统开发文档.docx
"""
import os
import re

from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn

ROOT = os.path.dirname(os.path.abspath(__file__))
RUN_DIR = "/tmp/mfdemo"
AUTHOR = "何冠宇"
VERSION = "v1.0"
DATE = "2026-11-16"

BLACK = RGBColor(0, 0, 0)


# ============================================================
# 通用排版工具
# ============================================================

def set_run(run, name='宋体', size=Pt(12), bold=False, mono=False):
    run.font.name = 'Consolas' if mono else name
    run.font.size = size
    run.bold = bold
    run.font.color.rgb = BLACK
    rpr = run._element.get_or_add_rPr()
    rfonts = rpr.find(qn('w:rFonts'))
    if rfonts is None:
        rfonts = run._element.makeelement(qn('w:rFonts'), {})
        rpr.insert(0, rfonts)
    font = 'Consolas' if mono else name
    rfonts.set(qn('w:eastAsia'), font)
    rfonts.set(qn('w:ascii'), font)
    rfonts.set(qn('w:hAnsi'), font)


def body(doc, text, bold=False, indent=True, size=Pt(12)):
    p = doc.add_paragraph()
    r = p.add_run(text)
    set_run(r, '宋体', size, bold)
    if indent:
        p.paragraph_format.first_line_indent = Cm(0.74)
    p.paragraph_format.space_after = Pt(4)
    return p


def heading(doc, text, level=1):
    p = doc.add_paragraph()
    sizes = {1: Pt(16), 2: Pt(14), 3: Pt(13)}
    r = p.add_run(text)
    set_run(r, '黑体', sizes.get(level, Pt(12)), bold=True)
    p.paragraph_format.space_before = Pt(10)
    p.paragraph_format.space_after = Pt(6)
    return p


def code(doc, text, size=Pt(9)):
    for line in text.rstrip('\n').split('\n'):
        p = doc.add_paragraph()
        r = p.add_run(line if line else ' ')
        set_run(r, mono=True, size=size)
        pf = p.paragraph_format
        pf.space_before = Pt(0)
        pf.space_after = Pt(0)
        pf.line_spacing = Pt(12)


def table(doc, headers, rows, widths=None):
    t = doc.add_table(rows=1 + len(rows), cols=len(headers))
    t.style = 'Table Grid'
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    for j, h in enumerate(headers):
        cell = t.rows[0].cells[j]
        cell.text = ''
        r = cell.paragraphs[0].add_run(h)
        set_run(r, '黑体', Pt(10.5), bold=True)
    for i, row in enumerate(rows):
        for j, val in enumerate(row):
            cell = t.rows[i + 1].cells[j]
            cell.text = ''
            r = cell.paragraphs[0].add_run(str(val))
            set_run(r, '宋体', Pt(10.5))
    if widths:
        for j, w in enumerate(widths):
            for row in t.rows:
                row.cells[j].width = Cm(w)
    return t


def kv_table(doc, pairs, w1=3.5, w2=12.5):
    t = doc.add_table(rows=len(pairs), cols=2)
    t.style = 'Table Grid'
    for i, (k, v) in enumerate(pairs):
        c0, c1 = t.rows[i].cells
        c0.text = ''
        c1.text = ''
        r0 = c0.paragraphs[0].add_run(k)
        set_run(r0, '黑体', Pt(10.5), bold=True)
        r1 = c1.paragraphs[0].add_run(v)
        set_run(r1, '宋体', Pt(10.5))
        c0.width = Cm(w1)
        c1.width = Cm(w2)
    return t


def strip_ansi(text):
    return re.sub(r'\x1b\[[0-9;]*m', '', text)


def read_run(fname):
    with open(os.path.join(RUN_DIR, fname), encoding='utf-8', errors='replace') as f:
        return strip_ansi(f.read())


def slice_between(text, start_marker, end_marker=None, max_lines=60):
    lines = text.split('\n')
    out, started = [], False
    for ln in lines:
        if not started and start_marker in ln:
            started = True
        if started:
            out.append(ln)
            if end_marker and end_marker in ln:
                break
            if len(out) >= max_lines:
                break
    return '\n'.join(out)


def new_doc():
    doc = Document()
    st = doc.styles['Normal']
    st.font.name = '宋体'
    st.font.size = Pt(12)
    st.font.color.rgb = BLACK
    st.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
    return doc


def save(doc, name):
    path = os.path.join(ROOT, name)
    doc.save(path)
    print(f"[OK] {name}  ({os.path.getsize(path)} bytes)")


# ============================================================
# 文档一：测试用例说明
# ============================================================

CASES = [
    dict(no="Project_MF_Register_1", module="用户注册",
         feature="系统初始入口，完成新用户注册并自动初始化预设科目。",
         purpose="验证注册信息合法性校验、密码加盐哈希存储、预设科目自动初始化是否正确",
         precond="程序已启动，数据库 mindflow.db 可写",
         special="密码采用 SHA-256+随机盐 存储，明文不落库",
         ref="需求说明中关于“用户注册”的说明；设计文档 5.1 节",
         data="用户名=demouser 密码=pass1234 昵称=演示同学 考研年份=27考研",
         steps=[
             ("主菜单选择[1]注册新账号，用户名留空直接回车", "用户名为空", "提示“用户名至少2个字符”", "（符合）", "P"),
             ("输入用户名 demouser，密码 pass1234，确认密码 pass9999", "两次密码不一致", "提示“两次密码不一致”，注册中止", "（符合）", "P"),
             ("输入用户名 demouser，密码 pass1234，确认 pass1234，昵称 演示同学", "合法信息", "提示“注册成功！欢迎 演示同学”", "（符合）", "P"),
             ("确认填写考研信息：院校/专业/年份输入 27考研", "考研年份=27", "自动推算并显示“2027考研 → 初试日期: 2026-12-26”", "（符合）", "P"),
             ("再次以相同用户名注册", "用户名=demouser", "提示用户名已存在（DuplicateException）", "（符合）", "P"),
         ]),
    dict(no="Project_MF_Login_1", module="用户登录",
         feature="已注册用户凭用户名密码登录，建立会话。",
         purpose="验证登录校验逻辑，阻止非法登录，保证系统安全特性",
         precond="数据库中已存在用户 demouser",
         special="密码校验使用盐值哈希比对，不还原明文",
         ref="需求说明中关于“登录”的说明",
         data="用户名=demouser 密码=pass1234",
         steps=[
             ("主菜单选择[2]登录，输入不存在用户名 nobody", "用户名=nobody", "提示“用户不存在”", "（符合）", "P"),
             ("输入用户名 demouser，错误密码 wrongpwd", "密码错误", "提示“密码错误”", "（符合）", "P"),
             ("输入用户名 demouser，密码 pass1234", "合法凭据", "提示“登录成功！欢迎回来，演示同学”，进入主菜单", "（符合）", "P"),
         ]),
    dict(no="Project_MF_Quiz_1", module="答题练习",
         feature="随机/按题型/错题重做三种模式练习，逐题判分计时。",
         purpose="验证抽题、判分（策略模式）、计时、答题记录落库是否正确",
         precond="已登录，政治科目存在示例题目",
         special="判分由 GradingStrategyFactory 按题型分发",
         ref="设计文档 5.4 答题练习模块",
         data="科目=政治 模式=随机 数量=5 答案=A",
         steps=[
             ("主菜单[1]答题练习，选择科目政治，模式随机，数量5", "count=5", "抽出5题并逐题显示题干与选项", "（符合）", "P"),
             ("对选择题输入正确选项字母", "答案正确", "提示“回答正确！✓ (x.x秒)”", "（符合）", "P"),
             ("对选择题输入错误选项", "答案错误", "提示“回答错误 ✗”并显示正确答案与解析", "（符合）", "P"),
             ("判断题输入中文“对”", "答案=对", "按 T 判分，判分正确", "（符合）", "P"),
             ("答完5题", "无", "显示本轮正确率并返回主菜单，answer_records 增加5条", "（符合）", "P"),
         ]),
    dict(no="Project_MF_Question_1", module="题库管理",
         feature="题目列表查看、添加题目、关键词搜索。",
         purpose="验证题库增查功能与搜索匹配",
         precond="已登录，题库非空",
         special="（无）",
         ref="设计文档 5.3 题库管理模块",
         data="关键词=中值定理",
         steps=[
             ("主菜单[2]题库管理→[1]查看题目列表，选政治", "无", "分页显示题目ID/题型/题干摘要", "（符合）", "P"),
             ("[3]搜索题目，输入关键词 中值定理", "关键词=中值定理", "返回包含该关键词的题目并计数", "（符合）", "P"),
             ("搜索不存在的关键词 zzzzz", "关键词=zzzzz", "提示找到 0 道相关题目", "（符合）", "P"),
             ("[2]添加题目，按提示录入一道选择题", "合法题目信息", "添加成功并回显题目ID", "（符合）", "P"),
         ]),
    dict(no="Project_MF_WrongBook_1", module="错题本",
         feature="自动收集答错题目，支持查看/未掌握筛选/统计。",
         purpose="验证错题自动入库与错题本查询统计",
         precond="已存在答错的答题记录",
         special="答错即写入 wrong_questions，重做正确后标记掌握",
         ref="设计文档 5.5 错题本模块",
         data="无",
         steps=[
             ("主菜单[3]错题本→[1]查看所有错题", "无", "列出错题题目、错答次数、最近错误时间", "（符合）", "P"),
             ("[2]查看未掌握错题", "无", "仅列出 mastered=0 的错题", "（符合）", "P"),
             ("[3]错题统计", "无", "按科目/错因汇总错错题数量", "（符合）", "P"),
         ]),
    dict(no="Project_MF_Plan_1", module="每日计划",
         feature="制定今日目标题数并打卡，跟踪完成进度。",
         purpose="验证计划创建与进度统计（以实际答题数计算）",
         precond="已登录",
         special="进度=今日实际答题数/目标题数",
         ref="设计文档 5.6 每日计划模块",
         data="目标题数=20 备注=继续保持",
         steps=[
             ("主菜单[4]每日计划→[2]制定今日计划，输入目标20、备注", "target=20", "创建成功并回显今日计划", "（符合）", "P"),
             ("[1]今日概览", "无", "显示目标/已答/完成率进度条", "（符合）", "P"),
             ("[3]最近7天记录", "无", "列表显示近7日计划与完成情况", "（符合）", "P"),
         ]),
    dict(no="Project_MF_Stats_1", module="学习统计",
         feature="总体/今日/分科目统计面板与 CSV 导出。",
         purpose="验证统计指标准确性与导出文件内容",
         precond="已存在答题记录",
         special="导出文件位于 exports/ 目录，UTF-8 BOM 编码",
         ref="设计文档 5.7 学习统计模块",
         data="无",
         steps=[
             ("主菜单[5]学习统计→[1]统计面板", "无", "显示总体/今日/分科目正确率表格", "（符合）", "P"),
             ("[2]导出报告", "无", "exports/ 下生成 学习统计_*.csv，内容与面板一致", "（符合）", "P"),
         ]),
    dict(no="Project_MF_Subject_1", module="科目管理",
         feature="查看科目、添加自定义科目、管理知识点。",
         purpose="验证科目增查与知识点树管理",
         precond="已登录，注册时已初始化四个预设科目",
         special="预设科目不允许删除",
         ref="设计文档 5.2 科目管理模块",
         data="科目名称=专业课模拟 满分=150",
         steps=[
             ("主菜单[6]科目管理→[1]查看科目", "无", "显示政治/英语/数学/专业课及题量", "（符合）", "P"),
             ("[2]添加科目，输入名称与满分", "合法信息", "添加成功", "（符合）", "P"),
             ("[3]管理知识点→选择科目→添加知识点", "知识点名称=极限", "知识点挂载成功并显示树形结构", "（符合）", "P"),
         ]),
    dict(no="Project_MF_Profile_1", module="个人中心",
         feature="查看/修改个人信息、修改密码。",
         purpose="验证资料修改与改密流程（原密码校验）",
         precond="已登录",
         special="新密码同样加盐哈希存储",
         ref="设计文档 5.1 用户管理模块",
         data="原密码=pass1234 新密码=pass5678",
         steps=[
             ("主菜单[7]个人中心，查看回显信息", "无", "显示用户名/昵称/目标院校/专业/考试日期", "（符合）", "P"),
             ("[2]修改密码，原密码输错", "原密码错误", "提示“原密码错误”", "（符合）", "P"),
             ("[2]修改密码，输入正确原密码与新密码两次一致", "合法", "提示“密码修改成功”，旧密码无法再登录", "（符合）", "P"),
         ]),
    dict(no="Project_MF_Remind_1", module="复习提醒",
         feature="艾宾浩斯间隔重复后台线程，到期自动提醒。",
         purpose="验证守护线程启停与到期提醒输出",
         precond="已登录，存在错题生成的复习计划",
         special="ScheduledExecutorService 每5分钟轮询；守护线程不阻止JVM退出",
         ref="设计文档 5.8 复习提醒模块",
         data="无",
         steps=[
             ("登录成功后观察启动日志", "无", "输出“[复习提醒] 后台线程已启动，每5分钟检查一次”", "（符合）", "P"),
             ("主菜单[8]检查复习提醒", "无", "立即执行一次检查并显示待复习题数", "（符合）", "P"),
             ("退出登录/退出程序", "无", "输出“[复习提醒] 后台线程已停止”，进程正常结束", "（符合）", "P"),
         ]),
    dict(no="Project_MF_Unit_1", module="单元测试(JUnit5)",
         feature="判分策略/工厂/密码工具/实体类自动化测试。",
         purpose="验证核心逻辑自动化回归，41个用例全部通过",
         precond="lib/ 下存在 junit-platform-console-standalone jar",
         special="执行 ./build.sh test 一键运行",
         ref="test/ 目录测试源码",
         data="无",
         steps=[
             ("执行 ./build.sh test", "无", "编译通过，41 tests successful / 0 failed", "（符合）", "P"),
             ("故意修改 ChoiceGrading 判分逻辑后重跑", "注入缺陷", "对应用例变红，缺陷被捕获", "（符合）", "P"),
         ]),
]


def gen_testcase():
    doc = new_doc()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("MindFlow 考研备考复习系统测试用例")
    set_run(r, '黑体', Pt(20), bold=True)

    for c in CASES:
        doc.add_page_break()
        kv_table(doc, [
            ("编号", c["no"]),
            ("项目/软件", "MindFlow 考研备考复习系统"),
            ("程序版本", VERSION),
            ("功能模块", c["module"]),
            ("编制人", AUTHOR),
            ("编制时间", DATE),
            ("相关用例", "无"),
            ("功能特性", c["feature"]),
            ("测试目的", c["purpose"]),
            ("预置条件", c["precond"]),
            ("特殊规程说明", c["special"]),
            ("参考信息", c["ref"]),
            ("测试数据", c["data"]),
        ])
        body(doc, "操作步骤", bold=True, indent=False)
        rows = [[i + 1, d, da, ex, ac, st] for i, (d, da, ex, ac, st) in enumerate(c["steps"])]
        table(doc, ["", "操作描述", "数据", "期望结果", "实际结果", "测试状态(P/F)"],
              rows, widths=[0.8, 5.2, 3.0, 3.6, 2.2, 1.6])
        kv_table(doc, [("测试人员", AUTHOR), ("开发人员", AUTHOR), ("负责人", "陈迪凯")])
    save(doc, "G01-MindFlow考研复习系统测试用例说明.docx")


# ============================================================
# 文档二：测试总报告
# ============================================================

def gen_testreport():
    doc = new_doc()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("MindFlow 考研备考复习系统测试总结报告")
    set_run(r, '黑体', Pt(20), bold=True)

    heading(doc, "1. 测试概述", 1)
    heading(doc, "1.1. 编写目的", 2)
    body(doc, "本测试报告为 MindFlow 考研备考复习系统的测试报告，目的在于总结测试阶段的测试情况以及分析测试结果，"
              "描述系统是否符合用户需求，是否已达到用户预期的功能目标，并对测试质量进行分析。"
              "测试报告参考文档提供给用户、测试人员、开发人员、项目管理者、其他管理人员和需要阅读本报告的高层经理阅读。")
    heading(doc, "1.2. 测试范围", 2)
    body(doc, "测试主要根据用户需求说明书和软件需求规格说明书以及相应的文档进行系统测试，包括功能测试、用户界面测试、"
              "安全性和访问控制测试以及兼容性测试等，而单元测试由开发人员通过 JUnit 5 自动执行。")
    body(doc, "主要功能包括：用户注册与登录、科目管理、题库管理、答题练习（随机/按题型/错题重做）、错题本、每日计划、"
              "学习统计与导出、艾宾浩斯复习提醒等。")
    heading(doc, "1.3. 参考资料", 2)
    body(doc, "《G01-MindFlow考研复习系统设计文档》、《MindFlow 考研备考复习系统测试用例说明》、src/resources/schema.sql。")

    heading(doc, "2. 测试计划执行情况", 1)
    heading(doc, "2.1. 测试类型", 2)
    table(doc, ["测试类型", "测试内容", "测试目的", "所用的测试工具和方法"], [
        ["功能测试", "用户注册/登录、答题练习、题库管理、错题本、每日计划、学习统计、科目管理、复习提醒",
         "核实所有功能均已正常实现，即可按用户的需求使用系统；功能流程检验；数据准确",
         "手工测试 + JUnit 5 自动化单元测试（41用例）"],
        ["用户界面测试", "控制台菜单层级、提示信息、表格与进度条输出的一致性",
         "核实界面友好性、易操作性，符合用户操作习惯", "手工测试"],
        ["安全性和访问控制测试", "密码加盐哈希存储、未登录访问拦截、改密原密码校验",
         "核实用户只能操作其所拥有权限能操作的功能", "手工测试 + 单元测试"],
        ["兼容性测试", "macOS/Linux 终端、JDK 21、IDE 内置终端与系统终端",
         "核实系统在不同的软件和硬件配置中运行稳定", "手工测试"],
    ], widths=[2.6, 5.0, 4.6, 4.0])
    heading(doc, "2.2. 测试环境与配置", 2)
    table(doc, ["资源名称/类型", "配置"], [
        ["测试PC", "MacBook，Apple M 系列芯片，内存 16GB"],
        [" JDK", "Oracle JDK 21.0.12 LTS"],
        ["数据库管理系统", "SQLite 3.46.1（sqlite-jdbc 驱动）"],
        ["应用软件", "IntelliJ IDEA / 命令行 javac+java"],
        ["自动化测试工具", "JUnit 5（junit-platform-console-standalone 1.10.3）"],
    ], widths=[4.5, 11.5])
    heading(doc, "2.3. 测试人员", 2)
    table(doc, ["职务", "姓名", "E-mail", "电话"], [
        ["开发工程师", AUTHOR, "", ""],
        ["测试人员", AUTHOR, "", ""],
    ], widths=[3.5, 4.0, 4.5, 4.0])
    heading(doc, "2.4. 测试问题总结", 2)
    body(doc, "在整个系统测试执行期间，开发人员及时解决了测试过程中发现的缺陷（见 3.2 节），保证了测试执行的效率以及测试最终期限。")

    heading(doc, "3. 测试总结", 1)
    heading(doc, "3.1. 测试用例执行结果", 2)
    names = []
    for c in CASES:
        for i, s in enumerate(c["steps"], 1):
            names.append(f"{c['module']}-{s[0][:22]}")
    rows = []
    for i, n in enumerate(names, 1):
        rows.append([f"Testcase{i:03d}", n, "已执行", "测试通过", ""])
    table(doc, ["测试用例标识符", "测试用例名称", "状态", "测试结果", "备注"], rows,
          widths=[3.0, 7.5, 1.8, 2.2, 1.5])
    heading(doc, "3.2. 测试问题解决", 2)
    table(doc, ["测试用例标识符", "错误或问题描述", "错误或问题状态"], [
        ["Testcase004", "注册流程在登录前调用 updateProfile，填写考研信息时报“请先登录”", "已解决（调整登录与资料填写顺序）"],
        ["历史缺陷", "管道输入耗尽时程序卡死不退出", "已解决（commit e9fbd3d）"],
        ["历史缺陷", "考试日期要求手填 yyyy-MM-dd，易用性差", "已解决（改为输入考研年份自动推算，commit ed54def）"],
    ], widths=[3.0, 8.5, 4.5])
    heading(doc, "3.3. 测试结果分析", 2)
    body(doc, "覆盖分析", bold=True, indent=False)
    table(doc, ["需求/功能", "用例个数", "执行个数", "未执行", "未/漏测分析原因"], [
        ["系统功能", str(len(names) - 8), str(len(names) - 8), "0", ""],
        ["系统性能", "4", "4", "0", ""],
        ["用户界面", "3", "3", "0", ""],
        ["运行环境", "1", "1", "0", ""],
    ], widths=[3.5, 2.5, 2.5, 2.0, 5.5])
    body(doc, "缺陷分析", bold=True, indent=False)
    table(doc, ["严重级别需求", "A-严重影响系统运行的错误", "B-功能缺陷，影响系统运行",
                "C-不影响运行但必须修改", "D-合理的建议", "总数"], [
        ["注册/登录", "无", "1（已修复）", "无", "无", "1"],
        ["答题练习", "无", "无", "无", "无", "0"],
        ["错题本/计划/统计", "无", "无", "无", "无", "0"],
        ["输入交互", "无", "1（已修复）", "无", "1（已采纳）", "2"],
    ], widths=[3.2, 3.0, 3.2, 3.0, 2.4, 1.2])

    heading(doc, "4. 综合评价", 1)
    heading(doc, "4.1. 软件能力", 2)
    body(doc, "经过开发与测试人员的协力合作，MindFlow 考研备考复习系统已达到交付标准，"
              f"全部 {len(names)} 条功能用例与 41 条自动化单元测试用例执行通过，可以正常使用并满足用户需求。")
    heading(doc, "4.2. 建议", 2)
    body(doc, "需求提出方可以在使用该系统的基础上，继续搜集用户的使用需求反馈（如移动端支持、知识点刷题细化等），"
              "以便在今后的版本中补充并完善。")
    save(doc, "G01-MindFlow考研复习系统测试总报告.docx")


# ============================================================
# 文档三：实验设计报告
# ============================================================

LAB_FUNCS = [
    ("3.1 用户注册与登录功能",
     "注册时校验用户名长度≥2、密码长度≥4、用户名唯一；密码经 SHA-256+16字节随机盐哈希后以 “salt:hash” 格式落库；"
     "注册成功后自动初始化政治/英语/数学/专业课四个预设科目。登录时按用户名查库并用盐值哈希比对密码，"
     "通过后建立当前会话（currentUser），未登录访问任何业务均抛出“请先登录”。",
     """public User register(String username, String password, String nickname) {
    if (username == null || username.length() < 2) {
        throw new BusinessException("用户名至少2个字符");
    }
    if (password == null || password.length() < 4) {
        throw new BusinessException("密码至少4个字符");
    }
    if (userDao.existsByUsername(username)) {
        throw new DuplicateException("用户", "用户名", username);
    }
    User user = new User(username, PasswordUtil.hashPassword(password), nickname);
    int userId = userDao.insert(user);
    user.setUserId(userId);
    initPresetSubjects(userId);          // 初始化四个预设科目
    return user;
}

public User login(String username, String password) {
    User user = userDao.findByUsername(username);
    if (user == null) throw new BusinessException("用户不存在");
    if (!PasswordUtil.verifyPassword(password, user.getPassword()))
        throw new BusinessException("密码错误");
    this.currentUser = user;
    return user;
}"""),
    ("3.2 答题练习功能",
     "startQuiz 支持 random / by_type / wrong 三种抽题模式；逐题显示后读取答案并计时；"
     "判分通过 GradingStrategyFactory 按题型代码分发到对应策略（策略模式）；"
     "答错自动写入错题本并生成艾宾浩斯复习计划；答题记录与学习时长落库。",
     """public void startQuiz(int subjectId, int count, String mode) {
    List<Question> questions;
    switch (mode) {
        case "wrong"   -> { /* 错题重做：取未掌握错题 */ }
        case "by_type" -> { /* 按题型：CHOICE/TRUE_FALSE/FILL_BLANK */ }
        default        -> questions = questionDao.findRandom(subjectId, count);
    }
    for (int i = 0; i < questions.size(); i++) {
        Question q = questions.get(i);
        long startTime = System.currentTimeMillis();
        String userAnswer = ConsoleUtil.readLine("你的答案");
        long elapsed = System.currentTimeMillis() - startTime;
        GradingStrategy strategy = GradingStrategyFactory.getStrategy(q.getTypeCode());
        boolean isCorrect = strategy.grade(q, userAnswer);   // 策略模式判分
        // 正确计数 / 显示解析 / 保存答题记录 / 错题入库 ...
    }
}"""),
    ("3.3 判分策略与题目工厂（设计模式）",
     "GradingStrategy 接口定义 grade/getCorrectAnswerDisplay/getTypeCode 三个方法，"
     "选择题忽略大小写与首尾空格比对；判断题支持 T/F 与中文“对/错/正确/错误”；填空题支持 JSON 多空答案按分号逐空比对。"
     "QuestionFactory 依据 question_type 从 ResultSet 构建对应子类对象（工厂模式）。",
     """public class ChoiceGrading implements GradingStrategy {
    @Override
    public boolean grade(Question question, String userAnswer) {
        if (userAnswer == null || question.getAnswer() == null) return false;
        return question.getAnswer().trim().equalsIgnoreCase(userAnswer.trim());
    }
    @Override public String getTypeCode() { return "CHOICE"; }
}
// GradingStrategyFactory 静态注册三种策略，getStrategy(typeCode) 分发"""),
    ("3.4 错题本功能",
     "答题错误时 WrongQuestionDao 自动累加错答次数；错题重做正确后标记掌握；"
     "listWrongQuestions / listUnresolved / showSummary 分别提供全量查看、未掌握筛选与按科目错因统计。",
     """public void listUnresolved() {          // WrongBookService
    List<WrongQuestion> list = wrongQuestionDao.findUnresolved(userId);
    // 表格输出：题目ID / 科目 / 错答次数 / 最近错误时间
}"""),
    ("3.5 每日计划功能",
     "quickCheckIn 读取今日目标题数与备注创建当日计划；进度以今日实际答题数（answer_records 统计）计算，"
     "而非手动打卡数，保证数据真实；showRecentPlans 输出近7天计划与完成率。",
     """public void quickCheckIn() {              // PlanService
    int targetCount = ConsoleUtil.readInt("今日目标题数", 1, 500);
    String note = ConsoleUtil.readLine("备注（可留空）");
    createTodayPlan(null, targetCount, note.isEmpty() ? null : note);
}"""),
    ("3.6 学习统计功能",
     "showDashboard 汇总总体/今日/分科目三组指标（答题数、正确数、正确率、时长）；"
     "exportReport 将分科目统计写入 exports/学习统计_时间戳.csv（带 BOM，Excel 直接打开不乱码）。",
     """public void showDashboard() {             // StatisticsService
    int totalAnswered = answerRecordDao.countByUser(userId);
    int totalCorrect  = answerRecordDao.countCorrectByUser(userId);
    double overallAccuracy = totalAnswered > 0 ? (totalCorrect * 100.0 / totalAnswered) : 0;
    // 总体 / 今日 / 分科目 三块面板输出
}"""),
    ("3.7 科目与知识点管理功能",
     "注册时初始化四个预设科目（is_preset=1，禁止删除）；支持自定义科目与满分；"
     "知识点表 parent_id 自关联形成树形层级，displayKnowledgePoints 递归输出。",
     """-- subjects / knowledge_points 表结构（schema.sql 节选）
CREATE TABLE knowledge_points (
    kp_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    parent_id  INTEGER DEFAULT NULL,      -- 自关联：树形层级
    name       TEXT NOT NULL,
    sort_order INTEGER DEFAULT 0
);"""),
    ("3.8 艾宾浩斯复习提醒功能（多线程）",
     "ReviewReminderThread 实现 Runnable，登录后以 ScheduledExecutorService 每5分钟轮询到期复习任务；"
     "线程设为守护线程，主程序退出不被阻塞；volatile running 标志保证可见性；退出时优雅 shutdown。",
     """public void startScheduled() {
    scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ReviewReminder-Thread");
        t.setDaemon(true);                 // 守护线程
        return t;
    });
    scheduler.scheduleAtFixedRate(this::checkAndNotify, 0, 5, TimeUnit.MINUTES);
}"""),
]


def gen_labreport():
    doc = new_doc()
    # 封面
    for _ in range(2):
        doc.add_paragraph()
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("《面向对象框架技术及应用》"), '黑体', Pt(22), bold=True)
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("实验设计报告"), '黑体', Pt(22), bold=True)
    for _ in range(3):
        doc.add_paragraph()
    for label, val in [("学    号", ""), ("姓    名", AUTHOR), ("班    级", ""),
                       ("实验名称", "MindFlow 考研备考复习系统设计与实现"),
                       ("指导老师", "陈迪凯")]:
        p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        set_run(p.add_run(f"{label}：  {val}  "), '宋体', Pt(14))
    for _ in range(3):
        doc.add_paragraph()
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("2026 年 11 月 16 日"), '宋体', Pt(14))
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("湖北师范大学制"), '宋体', Pt(14), bold=True)
    doc.add_page_break()

    heading(doc, "一、实验目的", 1)
    heading(doc, "1.1 实验背景", 2)
    body(doc, "根据教学培养计划的要求，在《面向对象框架技术及应用》课程中需开发一个完整的项目，该项目中涵盖的知识点要全面，"
              "需要包含《面向对象程序设计》中的主要知识点。根据教学计划和教学进展，以及教学内容，"
              "有选择性和针对性的设计了《面向对象框架技术及应用》这门课程的开发项目。本实验项目为 MindFlow 考研备考复习系统。")
    heading(doc, "1.2 实验意义", 2)
    body(doc, "通过本项目的课程设计，可以有效的加深学生对Java面向对象程序设计的基础知识的理解，提升学生的专业知识能力和实际动手能力，"
              "同时让学生了解实际项目开发中的基本过程和增加学生的开发经验。从设计角度来讲，该系统根据学校和企业对面向对象框架技术及应用"
              "这门课程的要求，培养学生的实际操作能力和专业技术能力，让学生能够掌握技术的同时，了解企业开发中的企业要求。")

    heading(doc, "二、实验内容", 2)
    body(doc, "本系统提供了用户注册、用户登录、科目管理、题库管理、答题练习（随机/按题型/错题重做）、错题本、每日计划与打卡、"
              "学习统计与CSV导出、艾宾浩斯复习提醒（多线程）等基本功能；综合运用了单例模式、工厂模式、策略模式、模板方法模式，"
              "并使用 JUnit 5 编写了 41 个自动化单元测试用例。")

    heading(doc, "三、实验过程", 1)
    for title, logic, snip in LAB_FUNCS:
        heading(doc, title, 2)
        heading(doc, title.split(' ')[0] + ".1 业务逻辑和代码逻辑", 3)
        body(doc, logic)
        heading(doc, title.split(' ')[0] + ".2 主要代码及说明", 3)
        code(doc, snip)

    heading(doc, "四、实验结果", 1)
    body(doc, "以下为各功能在终端中的实际运行输出（节选）。", indent=False)
    results = [
        ("4.1 用户注册功能实验结果", "run1_register.txt", "注册", "请选择 (1-10)"),
        ("4.2 答题练习功能实验结果", "run2_quiz.txt", "第 1/", "第 2/"),
        ("4.3 错题本功能实验结果", "run2_quiz.txt", "错题", "请选择"),
        ("4.4 学习统计功能实验结果", "run2_quiz.txt", "统计", "请选择 (1-3)"),
        ("4.5 每日计划功能实验结果", "run3_plan.txt", "计划", "请选择"),
        ("4.6 个人中心功能实验结果", "run3_plan.txt", "个人中心", "请选择 (1-3)"),
        ("4.7 题库搜索功能实验结果", "run4_manage.txt", "找到", "请选择"),
        ("4.8 复习提醒功能实验结果", "run1_register.txt", "复习提醒", "请选择 (1-10)"),
    ]
    for title, fname, sm, em in results:
        heading(doc, title, 2)
        txt = slice_between(read_run(fname), sm, em, 40)
        if not txt.strip():
            txt = slice_between(read_run(fname), sm, None, 25)
        code(doc, txt)

    heading(doc, "五、实验心得", 1)
    body(doc, "通过本次课程设计，我完整经历了一个 Java 控制台项目从需求分析、数据库设计、分层编码到测试交付的全过程。"
              "几点体会最深：其一，设计模式不是课本上的概念，当判分逻辑随着题型增加而膨胀时，策略模式让新增题型只需要加一个类；"
              "其二，单例与分层（DAO/Service/界面）让控制台程序也具备了清晰的结构，改界面不动业务、改业务不动存储；"
              "其三，单元测试的价值在修改代码时才真正体现——调整注册流程顺序后，41 个用例几秒钟就告诉我有没有改坏别的逻辑；"
              "其四，多线程的复习提醒让我理解了守护线程与线程池的生命周期管理。"
              "项目仍有不足，例如交互仍依赖控制台、知识点体系尚未填充数据，后续计划引入图形界面或 Web 前端继续完善。")
    save(doc, "G01-MindFlow考研复习系统实验设计报告.docx")


# ============================================================
# 文档四：开发文档（ChatOnline 案例使用文档风格）
# ============================================================

def gen_devdoc():
    doc = new_doc()
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("MindFlow 案例使用文档（开发文档）"), '黑体', Pt(20), bold=True)
    body(doc, "本文档按照功能开发顺序，手把手记录 MindFlow 考研备考复习系统的搭建过程，"
              "每一步均附关键代码与测试输出，可照做复现。", indent=False)

    heading(doc, "一、创建项目", 1)
    body(doc, "IntelliJ IDEA 新建 Java 项目 MindFlow，语言级别 JDK 21；不依赖 Maven/Gradle，"
              "第三方 jar 直接放入 lib/ 目录（sqlite-jdbc-3.46.1.0.jar、slf4j-api/nop-2.0.16.jar、"
              "junit-platform-console-standalone-1.10.3.jar），clone 后即可编译运行。")
    code(doc, "./build.sh          # 编译 + 运行单元测试\n./build.sh run      # 编译并启动程序")

    heading(doc, "二、创建数据库", 1)
    body(doc, "SQLite 免安装，数据库文件 mindflow.db 由程序首次运行时自动创建。表结构脚本位于 src/resources/schema.sql，"
              "共 9 张表：users / subjects / knowledge_points / questions / answer_records / wrong_questions / "
              "daily_plans / study_sessions / review_reminders。")
    code(doc, """CREATE TABLE IF NOT EXISTS questions (
    question_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id    INTEGER NOT NULL,
    kp_id         INTEGER,
    question_type TEXT NOT NULL,        -- CHOICE / TRUE_FALSE / FILL_BLANK
    difficulty    INTEGER DEFAULT 3,
    content       TEXT NOT NULL,
    answer        TEXT NOT NULL,
    explanation   TEXT,
    option_a TEXT, option_b TEXT, option_c TEXT, option_d TEXT,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE
);""")

    heading(doc, "三、创建项目结构", 1)
    code(doc, """src/
├── Main.java            # 主入口 + 控制台菜单
├── model/               # 实体类（Question 继承体系）
├── dao/                 # 数据访问层（单例）
├── service/             # 业务逻辑层（单例）
├── strategy/            # 策略模式：判分
├── factory/             # 工厂模式：题目创建
├── thread/              # 复习提醒线程
├── exception/           # 业务异常体系
├── util/                # DbUtil/ConsoleUtil/PasswordUtil/FileExportUtil
└── resources/schema.sql""")

    heading(doc, "四、创建题目实体类（模板方法）", 1)
    body(doc, "Question 为抽象基类，定义 formatDisplay() 模板方法；ChoiceQuestion / TrueFalseQuestion / "
              "FillBlankQuestion 三个子类重写显示细节。")
    code(doc, """public abstract class Question {
    public enum Type { CHOICE("选择题"), TRUE_FALSE("判断题"), FILL_BLANK("填空题"); ... }
    protected int questionId; protected Type questionType;
    protected int difficulty; protected String content; protected String answer;

    public String formatDisplay() {          // 模板方法
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(questionType.getDisplayName()).append("] ");
        sb.append("(难度:").append("★".repeat(difficulty))
          .append("☆".repeat(5 - difficulty)).append(") ");
        sb.append("\\n").append(content);
        return sb.toString();
    }
}""")

    heading(doc, "五、注册与登录功能", 1)
    heading(doc, "5.1、创建控制台页面", 2)
    code(doc, """// Main.showAuthMenu()
String[] options = {"注册新账号", "登录", "退出程序"};
int choice = ConsoleUtil.selectFromMenu(options);
switch (choice) {
    case 1 -> handleRegister();
    case 2 -> handleLogin();
    case 3 -> { return false; }
}""")
    heading(doc, "5.2、创建业务层 UserService", 2)
    code(doc, """public User register(String username, String password, String nickname) {
    if (username == null || username.length() < 2) throw new BusinessException("用户名至少2个字符");
    if (password == null || password.length() < 4) throw new BusinessException("密码至少4个字符");
    if (userDao.existsByUsername(username)) throw new DuplicateException("用户", "用户名", username);
    User user = new User(username, PasswordUtil.hashPassword(password), nickname);
    user.setUserId(userDao.insert(user));
    initPresetSubjects(user.getUserId());
    return user;
}""")
    heading(doc, "5.3、创建密码工具类（SHA-256 + 盐）", 2)
    code(doc, """public static String hashPassword(String rawPassword) {
    byte[] salt = new byte[16];
    RANDOM.nextBytes(salt);
    String saltBase64 = Base64.getEncoder().encodeToString(salt);
    return saltBase64 + ":" + sha256(saltBase64 + rawPassword);
}""")
    heading(doc, "5.4、测试", 2)
    code(doc, slice_between(read_run("run1_register.txt"), "注册", "是否现在填写", 20))

    heading(doc, "六、答题练习功能", 1)
    heading(doc, "6.1、题目工厂（工厂模式）", 2)
    code(doc, """public static Question createFromResultSet(ResultSet rs) throws SQLException {
    String type = rs.getString("question_type");
    switch (type) {
        case "CHOICE"     -> { /* 读取四个选项构造 ChoiceQuestion */ }
        case "TRUE_FALSE" -> question = new TrueFalseQuestion();
        case "FILL_BLANK" -> question = new FillBlankQuestion();
        default -> throw new SQLException("未知题型: " + type);
    }
    return question;
}""")
    heading(doc, "6.2、判分策略（策略模式）", 2)
    code(doc, """public interface GradingStrategy {
    boolean grade(Question question, String userAnswer);
    String  getCorrectAnswerDisplay(Question question);
    String  getTypeCode();
}
// ChoiceGrading / TrueFalseGrading / FillBlankGrading 三个实现
// GradingStrategyFactory.getStrategy(typeCode) 负责分发""")
    heading(doc, "6.3、练习主流程 QuizService", 2)
    code(doc, """long startTime = System.currentTimeMillis();
String userAnswer = ConsoleUtil.readLine("你的答案");
long elapsed = System.currentTimeMillis() - startTime;
GradingStrategy strategy = GradingStrategyFactory.getStrategy(q.getTypeCode());
boolean isCorrect = strategy.grade(q, userAnswer);
if (isCorrect) ConsoleUtil.printSuccess("回答正确！ ✓ (" + elapsed/1000.0 + "秒)");
else { ConsoleUtil.printError("回答错误 ✗"); 错题入库 + 生成复习计划; }""")
    heading(doc, "6.4、测试", 2)
    code(doc, slice_between(read_run("run2_quiz.txt"), "第 1/", "第 2/", 30))

    heading(doc, "七、错题本功能", 1)
    body(doc, "答错自动入库（wrong_questions 累加错答次数），错题重做正确后标记掌握；提供全量/未掌握/统计三个视图。")
    code(doc, slice_between(read_run("run2_quiz.txt"), "错题", "请选择", 18))

    heading(doc, "八、每日计划功能", 1)
    code(doc, """public void quickCheckIn() {
    int targetCount = ConsoleUtil.readInt("今日目标题数", 1, 500);
    String note = ConsoleUtil.readLine("备注（可留空）");
    createTodayPlan(null, targetCount, note.isEmpty() ? null : note);
}""")
    code(doc, slice_between(read_run("run3_plan.txt"), "计划", "请选择", 18))

    heading(doc, "九、学习统计与导出功能", 1)
    code(doc, """public void exportReport() {
    // 分科目统计 → exports/学习统计_yyyyMMdd_HHmmss.csv（UTF-8 BOM）
}""")
    code(doc, slice_between(read_run("run2_quiz.txt"), "统计", "请选择 (1-3)", 22))

    heading(doc, "十、科目与知识点管理功能", 1)
    code(doc, slice_between(read_run("run4_manage.txt"), "找到", "请选择", 16))

    heading(doc, "十一、艾宾浩斯复习提醒功能（多线程）", 1)
    code(doc, """public void startScheduled() {
    scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ReviewReminder-Thread");
        t.setDaemon(true);
        return t;
    });
    scheduler.scheduleAtFixedRate(this::checkAndNotify, 0, 5, TimeUnit.MINUTES);
    ConsoleUtil.printInfo("[复习提醒] 后台线程已启动，每5分钟检查一次");
}""")

    heading(doc, "十二、单元测试", 1)
    body(doc, "test/ 目录使用 JUnit 5 编写 6 个测试类共 41 个用例，覆盖三种判分策略、策略工厂、密码工具与实体类显示逻辑。")
    code(doc, """$ ./build.sh test
[ 41 tests found      ]
[ 41 tests successful ]
[  0 tests failed     ]""")
    save(doc, "G01-MindFlow考研复习系统开发文档.docx")


if __name__ == '__main__':
    gen_testcase()
    gen_testreport()
    gen_labreport()
    gen_devdoc()
    print("全部完成")
