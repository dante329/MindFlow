package service;

import dao.QuestionDao;
import dao.SubjectDao;
import model.ChoiceQuestion;
import model.FillBlankQuestion;
import model.Question;
import model.Subject;
import model.TrueFalseQuestion;
import util.ConsoleUtil;

import java.util.List;

/**
 * 示例数据初始化服务
 * 首次使用时自动导入考研各科示例题目
 * 包含政治、英语、数学三科的选择题、判断题、填空题
 */
public class SampleDataService {

    private static final SampleDataService INSTANCE = new SampleDataService();
    private final QuestionDao questionDao = QuestionDao.getInstance();
    private final SubjectDao subjectDao = SubjectDao.getInstance();

    public static SampleDataService getInstance() {
        return INSTANCE;
    }

    /**
     * 检查并导入示例数据
     * 只在题库为空时导入，避免重复
     */
    public void initIfEmpty() {
        int userId = UserService.getInstance().getCurrentUser().getUserId();
        List<Subject> subjects = subjectDao.findByUserId(userId);

        boolean hasData = false;
        for (Subject s : subjects) {
            if (questionDao.countBySubject(s.getSubjectId()) > 0) {
                hasData = true;
                break;
            }
        }

        if (hasData) {
            return; // 已有数据，跳过
        }

        ConsoleUtil.printInfo("检测到题库为空，正在导入示例题目...");

        // 找到各科目的 ID
        Integer politicsId = null, englishId = null, mathId = null;
        for (Subject s : subjects) {
            switch (s.getName()) {
                case "政治" -> politicsId = s.getSubjectId();
                case "英语" -> englishId = s.getSubjectId();
                case "数学" -> mathId = s.getSubjectId();
            }
        }

        int count = 0;
        if (politicsId != null) {
            count += loadPoliticsQuestions(politicsId);
        }
        if (englishId != null) {
            count += loadEnglishQuestions(englishId);
        }
        if (mathId != null) {
            count += loadMathQuestions(mathId);
        }

        ConsoleUtil.printSuccess("示例数据导入完成！共导入 " + count + " 道题目");
    }

    // ==================== 政治题目 ====================

    private int loadPoliticsQuestions(int subjectId) {
        int count = 0;

        // --- 选择题 ---
        count += addChoice(subjectId, 2,
                "马克思主义哲学的直接理论来源是",
                "古希腊哲学", "德国古典哲学", "英国古典政治经济学", "法国空想社会主义",
                "B", "马克思主义哲学的直接理论来源是德国古典哲学，主要是黑格尔的辩证法和费尔巴哈的唯物主义。");

        count += addChoice(subjectId, 2,
                "唯物辩证法的总特征是",
                "联系的观点和发展的观点", "对立统一的观点", "量变和质变的观点", "否定之否定的观点",
                "A", "联系和发展是唯物辩证法的总特征，对立统一是唯物辩证法的实质和核心。");

        count += addChoice(subjectId, 3,
                "社会存在决定社会意识，社会意识是社会存在的反映，这是",
                "历史唯物主义的基本原理", "历史唯心主义的基本原理", "机械唯物主义的基本原理", "辩证唯物主义的基本原理",
                "A", "社会存在决定社会意识是历史唯物主义的根本观点，是区分历史唯物主义和历史唯心主义的根本标准。");

        count += addChoice(subjectId, 2,
                "商品的二因素是",
                "使用价值和价值", "具体劳动和抽象劳动", "私人劳动和社会劳动", "必要劳动和剩余劳动",
                "A", "商品具有使用价值和价值两个因素。使用价值是商品的自然属性，价值是商品的社会属性。");

        count += addChoice(subjectId, 3,
                "剩余价值率是",
                "剩余价值与可变资本的比率", "剩余价值与不变资本的比率", "剩余价值与全部资本的比率", "利润与全部资本的比率",
                "A", "剩余价值率 m' = m/v，即剩余价值与可变资本的比率，反映资本家对工人的剥削程度。");

        count += addChoice(subjectId, 2,
                "新民主主义革命的总路线是",
                "无产阶级领导的，人民大众的，反对帝国主义、封建主义和官僚资本主义的革命",
                "资产阶级领导的，反对封建主义的革命",
                "无产阶级领导的，反对帝国主义的革命",
                "农民阶级领导的，反对封建主义和官僚资本主义的革命",
                "A", "1948年毛泽东在《在晋绥干部会议上的讲话》中完整提出了新民主主义革命的总路线。");

        count += addChoice(subjectId, 3,
                "中国特色社会主义进入新时代，我国社会主要矛盾已经转化为",
                "人民日益增长的美好生活需要和不平衡不充分的发展之间的矛盾",
                "人民日益增长的物质文化需要同落后的社会生产之间的矛盾",
                "无产阶级同资产阶级之间的矛盾",
                "经济发展同环境保护之间的矛盾",
                "A", "党的十九大报告指出，中国特色社会主义进入新时代，我国社会主要矛盾已经转化为人民日益增长的美好生活需要和不平衡不充分的发展之间的矛盾。");

        count += addChoice(subjectId, 2,
                "实践作为检验真理的唯一标准，是由",
                "真理的本性和实践的特点决定的", "真理的客观性决定的", "实践的客观性决定的", "真理的相对性决定的",
                "A", "实践之所以是检验真理的唯一标准，是由真理的本性（主观与客观相符合）和实践的特点（直接现实性）决定的。");

        count += addChoice(subjectId, 4,
                "资本有机构成是指",
                "由资本技术构成决定并反映技术构成变化的资本价值构成",
                "不变资本与可变资本的比例",
                "固定资本与流动资本的比例",
                "生产资本与流通资本的比例",
                "A", "资本有机构成 c:v 是由资本技术构成决定并反映技术构成变化的资本价值构成。注意区分资本技术构成、价值构成和有机构成三个概念。");

        count += addChoice(subjectId, 3,
                "习近平新时代中国特色社会主义思想的核心要义是",
                "坚持和发展中国特色社会主义", "实现中华民族伟大复兴", "全面深化改革", "全面依法治国",
                "A", "坚持和发展中国特色社会主义是习近平新时代中国特色社会主义思想的核心要义。");

        // --- 判断题 ---
        count += addTrueFalse(subjectId, 1,
                "物质是第一性的，意识是第二性的，物质决定意识。", "T",
                "这是唯物主义的基本观点，物质是世界的本原，意识是物质的产物和反映。");

        count += addTrueFalse(subjectId, 2,
                "矛盾的同一性是无条件的、绝对的。", "F",
                "矛盾的斗争性是无条件的、绝对的，矛盾的同一性是有条件的、相对的。");

        count += addTrueFalse(subjectId, 2,
                "价值规律的表现形式是价格围绕价值上下波动。", "T",
                "受供求关系影响，价格围绕价值上下波动是价值规律的表现形式。");

        count += addTrueFalse(subjectId, 3,
                "新民主主义革命属于世界无产阶级社会主义革命的一部分。", "T",
                "十月革命后，殖民地半殖民地的民族解放运动已成为世界无产阶级社会主义革命的一部分。");

        count += addTrueFalse(subjectId, 2,
                "改革是中国的第二次革命。", "T",
                "邓小平指出，改革是中国的第二次革命。改革不是对原有体制的细枝末节的修补，而是对体制的根本性变革。");

        // --- 填空题 ---
        count += addFillBlank(subjectId, 2,
                "马克思主义的三个组成部分是____、____和____。",
                "{\"1\":\"马克思主义哲学\",\"2\":\"马克思主义政治经济学\",\"3\":\"科学社会主义\"}",
                "马克思主义哲学、马克思主义政治经济学、科学社会主义是马克思主义的三个基本组成部分。");

        count += addFillBlank(subjectId, 3,
                "社会主义核心价值观在国家层面的价值目标是____、____、____、____。",
                "{\"1\":\"富强\",\"2\":\"民主\",\"3\":\"文明\",\"4\":\"和谐\"}",
                "国家层面：富强、民主、文明、和谐；社会层面：自由、平等、公正、法治；个人层面：爱国、敬业、诚信、友善。");

        return count;
    }

    // ==================== 英语题目 ====================

    private int loadEnglishQuestions(int subjectId) {
        int count = 0;

        // --- 选择题（词汇语法） ---
        count += addChoice(subjectId, 2,
                "The committee ____ divided in their opinions on this issue.\n(委员会在这个问题上意见不一致。)",
                "is", "are", "was", "were",
                "D", "committee 是集合名词，当强调成员个体时用复数动词。此处 'their opinions' 表明强调个体，且语境为过去时，故选 were。");

        count += addChoice(subjectId, 2,
                "Not until he retired from teaching three years ago ____ having a holiday abroad.\n(直到三年前退休，他才考虑去国外度假。)",
                "he had considered", "had he considered", "he considered", "did he consider",
                "D", "Not until 置于句首时，主句需要部分倒装。时间状语 three years ago 表明用一般过去时，故用 did he consider。");

        count += addChoice(subjectId, 3,
                "The book is said ____ into at least twenty foreign languages.\n(据说这本书已被翻译成至少二十种外语。)",
                "to translate", "to be translated", "to have been translated", "having been translated",
                "C", "be said to do 结构。翻译发生在据说之前，需用不定式的完成式；书是被翻译的，需用被动语态。故选 to have been translated。");

        count += addChoice(subjectId, 2,
                "It was not until midnight ____ they reached the camp site.\n(直到午夜他们才到达营地。)",
                "that", "when", "while", "as",
                "A", "这是 It was not until... that... 的强调句型，强调时间状语 not until midnight。");

        count += addChoice(subjectId, 3,
                "____ he works hard, I don't mind when he finishes the experiment.\n(只要他努力工作，我不介意他什么时候完成实验。)",
                "As soon as", "As well as", "As far as", "So long as",
                "D", "so long as = as long as，意为'只要'，引导条件状语从句。as soon as 一...就；as well as 也；as far as 就...而言。");

        count += addChoice(subjectId, 2,
                "The phenomenon of global warming has become a ____ concern for all nations.\n(全球变暖现象已成为所有国家____关注的问题。)",
                "common", "mutual", "universal", "general",
                "C", "universal concern 表示'普遍关注'，强调全世界所有国家都关注。common 常见的；mutual 相互的；general 一般的。");

        count += addChoice(subjectId, 4,
                "Hardly ____ when the rain began to pour down.\n(他刚出门，雨就倾盆而下。)",
                "he went out", "did he go out", "had he gone out", "he had gone out",
                "C", "Hardly... when... 句型中，hardly 后的主句用过去完成时且需倒装。故选 had he gone out。");

        count += addChoice(subjectId, 3,
                "The reason why he was late is ____ he missed the early bus.\n(他迟到的原因是他错过了早班车。)",
                "because", "that", "why", "for",
                "B", "The reason why... is that... 是固定句型，表语从句用 that 引导，不能用 because。");

        count += addChoice(subjectId, 2,
                "She insisted that he ____ present at the meeting.\n(她坚持要求他出席会议。)",
                "is", "was", "be", "would be",
                "C", "insist 表示'坚持要求'时，宾语从句用虚拟语气 (should) + 动词原形，should 可省略。");

        count += addChoice(subjectId, 3,
                "____ is known to all, the earth revolves around the sun.\n(众所周知，地球绕太阳转。)",
                "It", "As", "Which", "What",
                "B", "As is known to all 是非限制性定语从句，as 指代后面整个主句的内容，可置于句首。which 引导的非限制性定语从句不能置于句首。");

        // --- 判断题（语法正误判断） ---
        count += addTrueFalse(subjectId, 2,
                "\"Each of the students have their own textbook.\" 这句话语法正确。", "F",
                "Each of the students 作主语时谓语动词用单数，应改为 has。");

        count += addTrueFalse(subjectId, 2,
                "\"I look forward to hear from you soon.\" 这句话语法正确。", "F",
                "look forward to 中的 to 是介词，后面应接动名词 hearing，不是动词原形 hear。");

        count += addTrueFalse(subjectId, 3,
                "\"The more you practice, the better you will become.\" 这句话语法正确。", "T",
                "The + 比较级..., the + 比较级... 是正确的句型，表示'越...越...'。");

        count += addTrueFalse(subjectId, 2,
                "\"Neither John nor his friends was invited to the party.\" 这句话语法正确。", "F",
                "Neither... nor... 连接主语时，谓语动词遵循就近原则，his friends 是复数，应用 were。");

        count += addTrueFalse(subjectId, 3,
                "\"Had I known about the meeting, I would have attended it.\" 这句话语法正确。", "T",
                "这是虚拟语气的倒装形式，省略了 if，将 had 提前。等同于 If I had known about the meeting, I would have attended it.");

        // --- 填空题 ---
        count += addFillBlank(subjectId, 2,
                "He suggested that we ____ (start) the project immediately.",
                "{\"1\":\"(should) start\"}",
                "suggest 表示'建议'时，宾语从句用虚拟语气 (should) + 动词原形。填 start 或 should start 均可。");

        count += addFillBlank(subjectId, 3,
                "Not only ____ he ____ (finish) his homework, but he also helped his sister with hers.",
                "{\"1\":\"did\",\"2\":\"finish\"}",
                "Not only 置于句首时，该分句需部分倒装。根据后半句 helped 可知用一般过去时，故用 did... finish。");

        return count;
    }

    // ==================== 数学题目 ====================

    private int loadMathQuestions(int subjectId) {
        int count = 0;

        // --- 选择题 ---
        count += addChoice(subjectId, 2,
                "函数 f(x) = x³ - 3x + 1 在区间 [-2, 2] 上的最大值为",
                "1", "3", "-1", "5",
                "B", "f'(x) = 3x² - 3 = 3(x+1)(x-1)，令 f'(x)=0 得 x=±1。f(-2)=-1, f(-1)=3, f(1)=-1, f(2)=3。最大值为 3。");

        count += addChoice(subjectId, 3,
                "设 A 为 3 阶矩阵，|A| = 2，则 |2A⁻¹| =",
                "4", "1", "2", "8",
                "A", "|2A⁻¹| = 2³ · |A⁻¹| = 8 · (1/|A|) = 8 · (1/2) = 4。n 阶矩阵 |kA| = kⁿ|A|。");

        count += addChoice(subjectId, 2,
                "极限 lim(x→0) sin(3x)/x =",
                "1", "3", "0", "1/3",
                "B", "lim(x→0) sin(3x)/x = lim(x→0) 3·sin(3x)/(3x) = 3·1 = 3。利用重要极限 lim(x→0) sinx/x = 1。");

        count += addChoice(subjectId, 3,
                "设随机变量 X ~ N(0, 1)，则 P(|X| < 1) 约为",
                "0.6826", "0.9544", "0.9974", "0.5000",
                "A", "标准正态分布中，P(|X| < 1) = P(-1 < X < 1) ≈ 0.6826，即约68.26%的数据落在均值±1个标准差内。");

        count += addChoice(subjectId, 4,
                "级数 Σ(n=1 to ∞) (-1)ⁿ/n 是",
                "绝对收敛", "条件收敛", "发散", "无法判断",
                "B", "这是交错调和级数。由莱布尼茨判别法知其收敛，但 Σ1/n 发散，故为条件收敛。");

        // --- 判断题 ---
        count += addTrueFalse(subjectId, 2,
                "若函数 f(x) 在点 x₀ 处可导，则 f(x) 在 x₀ 处一定连续。", "T",
                "可导必连续，这是基本定理。但连续不一定可导，如 f(x)=|x| 在 x=0 处连续但不可导。");

        count += addTrueFalse(subjectId, 3,
                "若级数 Σaₙ 收敛，则 lim(n→∞) aₙ = 0。", "T",
                "这是级数收敛的必要条件。但反过来不成立，如调和级数 Σ1/n 的通项趋于0但级数发散。");

        count += addTrueFalse(subjectId, 2,
                "矩阵 A 可逆的充要条件是 |A| ≠ 0。", "T",
                "方阵 A 可逆（非奇异）的充要条件是其行列式不等于零。");

        count += addTrueFalse(subjectId, 3,
                "若 f''(x₀) = 0，则 x₀ 一定是 f(x) 的拐点。", "F",
                "f''(x₀) = 0 只是拐点的必要条件，不是充分条件。还需要 f''(x) 在 x₀ 两侧变号。例如 f(x) = x⁴，f''(0) = 0 但 x=0 不是拐点。");

        // --- 填空题 ---
        count += addFillBlank(subjectId, 2,
                "∫(2x + cosx)dx = ____",
                "{\"1\":\"x² + sinx + C\"}",
                "逐项积分：∫2x dx = x²，∫cosx dx = sinx，加上积分常数 C。");

        count += addFillBlank(subjectId, 3,
                "设 f(x) = eˣ · sinx，则 f'(x) = ____",
                "{\"1\":\"eˣ(sinx + cosx)\"}",
                "乘法法则：f'(x) = eˣ·sinx + eˣ·cosx = eˣ(sinx + cosx)。");

        return count;
    }

    // ==================== 辅助方法 ====================

    private int addChoice(int subjectId, int difficulty, String content,
                           String optA, String optB, String optC, String optD,
                           String answer, String explanation) {
        ChoiceQuestion q = new ChoiceQuestion(subjectId, difficulty, content, optA, optB, optC, optD, answer);
        q.setExplanation(explanation);
        questionDao.insert(q);
        return 1;
    }

    private int addTrueFalse(int subjectId, int difficulty, String content, String answer, String explanation) {
        TrueFalseQuestion q = new TrueFalseQuestion(subjectId, difficulty, content, answer);
        q.setExplanation(explanation);
        questionDao.insert(q);
        return 1;
    }

    private int addFillBlank(int subjectId, int difficulty, String content, String answer, String explanation) {
        FillBlankQuestion q = new FillBlankQuestion(subjectId, difficulty, content, answer);
        q.setExplanation(explanation);
        questionDao.insert(q);
        return 1;
    }
}
