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
        Integer politicsId = null, englishId = null, mathId = null, majorId = null;
        for (Subject s : subjects) {
            switch (s.getName()) {
                case "政治" -> politicsId = s.getSubjectId();
                case "英语" -> englishId = s.getSubjectId();
                case "数学" -> mathId = s.getSubjectId();
                case "专业课" -> majorId = s.getSubjectId();
            }
        }

        int count = 0;
        if (politicsId != null) {
            count += loadPoliticsQuestions(politicsId);
            count += loadPoliticsExtra(politicsId);
        }
        if (englishId != null) {
            count += loadEnglishQuestions(englishId);
            count += loadEnglishExtra(englishId);
        }
        if (mathId != null) {
            count += loadMathQuestions(mathId);
            count += loadMathExtra(mathId);
        }
        if (majorId != null) {
            count += loadMajorQuestions(majorId);
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

        // --- 补充选择题 ---
        count += addChoice(subjectId, 2,
                "哲学的基本问题是",
                "思维和存在的关系问题", "运动和静止的关系问题", "量变和质变的关系问题", "理论和实践的关系问题",
                "A", "恩格斯指出，全部哲学特别是近代哲学的重大基本问题是思维和存在的关系问题。");

        count += addChoice(subjectId, 3,
                "认识的两次飞跃是指",
                "从感性认识到理性认识，再从理性认识到实践", "从实践到认识，再从认识到实践", "从现象到本质，再从本质到现象", "从具体到抽象，再从抽象到具体",
                "A", "第一次飞跃是从感性认识到理性认识，第二次飞跃是从理性认识到实践。第二次飞跃比第一次更重要。");

        count += addChoice(subjectId, 2,
                "生产力中最活跃的因素是",
                "劳动者", "劳动资料", "劳动对象", "科学技术",
                "A", "劳动者是生产力中最活跃的因素。科学技术是第一生产力，但需要通过劳动者来发挥作用。");

        count += addChoice(subjectId, 3,
                "我国的基本经济制度是",
                "公有制为主体、多种所有制经济共同发展", "全民所有制经济", "集体所有制经济", "私有制经济",
                "A", "党的十九届四中全会将公有制为主体、多种所有制经济共同发展，按劳分配为主体、多种分配方式并存，社会主义市场经济体制都列为基本经济制度。");

        count += addChoice(subjectId, 2,
                "毛泽东思想的活的灵魂是",
                "实事求是、群众路线、独立自主", "解放思想、实事求是、与时俱进", "理论联系实际、密切联系群众、批评与自我批评", "统一战线、武装斗争、党的建设",
                "A", "实事求是、群众路线、独立自主是毛泽东思想的活的灵魂的三个基本方面。注意D选项是三大法宝。");

        count += addChoice(subjectId, 4,
                "垄断利润的来源是",
                "工人创造的剩余价值和其他劳动者创造的一部分价值", "垄断资本家的先进管理", "垄断组织的技术优势", "市场价格波动",
                "A", "垄断利润归根到底来源于工人创造的剩余价值和其他劳动者创造的一部分价值。");

        count += addChoice(subjectId, 3,
                "\"三个代表\"重要思想的核心观点是",
                "中国共产党必须始终代表中国先进生产力的发展要求、代表中国先进文化的前进方向、代表中国最广大人民的根本利益",
                "坚持以人为本、全面协调可持续发展", "建设社会主义和谐社会", "全面建设小康社会",
                "A", "这是江泽民提出的，2002年写入党章。");

        count += addChoice(subjectId, 2,
                "科学发展观的第一要义是",
                "发展", "以人为本", "全面协调可持续", "统筹兼顾",
                "A", "科学发展观：第一要义是发展，核心是以人为本，基本要求是全面协调可持续，根本方法是统筹兼顾。");

        // --- 补充判断题 ---
        count += addTrueFalse(subjectId, 2,
                "否定之否定规律揭示了事物发展的方向和道路。", "T",
                "否定之否定规律揭示了事物发展的前进性与曲折性的统一，表明事物发展的方向是前进的，道路是曲折的。");

        count += addTrueFalse(subjectId, 3,
                "上层建筑对经济基础总是起促进作用。", "F",
                "当上层建筑为先进的经济基础服务时起促进作用，为落后的经济基础服务时起阻碍作用。");

        count += addTrueFalse(subjectId, 2,
                "帝国主义是资本主义的最高阶段。", "T",
                "列宁在《帝国主义是资本主义的最高阶段》中指出，帝国主义是垄断的、腐朽的、垂死的资本主义。");

        // --- 补充填空题 ---
        count += addFillBlank(subjectId, 2,
                "党的三大优良作风是____、____和____。",
                "{\"1\":\"理论联系实际\",\"2\":\"密切联系群众\",\"3\":\"批评与自我批评\"}",
                "毛泽东在党的七大上总结了党的三大优良作风。");

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

        // --- 补充选择题 ---
        count += addChoice(subjectId, 2,
                "I would rather you ____ anything about it for the time being.\n(我宁愿你暂时不要对此事做任何表态。)",
                "don't do", "didn't do", "won't do", "not do",
                "B", "would rather 后接宾语从句时用虚拟语气，对现在/将来的情况用一般过去时。");

        count += addChoice(subjectId, 3,
                "It is high time that we ____ a meeting to discuss the problem.\n(我们早该开会讨论这个问题了。)",
                "hold", "held", "have held", "will hold",
                "B", "It is (high/about) time that... 后面的从句用虚拟语气，动词用一般过去时或 should + 动词原形。");

        count += addChoice(subjectId, 2,
                "The number of students in our school ____ increasing year by year.\n(我们学校的学生人数逐年增加。)",
                "is", "are", "were", "have been",
                "A", "the number of + 复数名词作主语时，谓语动词用单数。注意区分 a number of（许多）后接复数动词。");

        count += addChoice(subjectId, 3,
                "______ she couldn't understand was _____ fewer and fewer students showed interest in her lessons.\n(她无法理解的是为什么越来越少的学生对她的课感兴趣。)",
                "What; why", "That; what", "What; because", "Why; that",
                "A", "第一个空是主语从句缺宾语用 what，第二个空是表语从句表示原因用 why。");

        count += addChoice(subjectId, 4,
                "Under no circumstances ____ leave the door unlocked.\n(在任何情况下都不要忘记锁门。)",
                "you should", "should you", "you will", "will you",
                "B", "Under no circumstances 是否定短语置于句首，需要部分倒装，将情态动词 should 提前。");

        count += addChoice(subjectId, 2,
                "He is the only one of the students who ____ been to Beijing.\n(他是学生中唯一去过北京的人。)",
                "has", "have", "had", "having",
                "A", "the only one of + 复数名词 + who 定语从句，who 指代 the only one（单数），谓语用 has。");

        count += addChoice(subjectId, 3,
                "The professor, along with his assistants, ____ working on the project day and night.\n(教授和他的助手们日夜不停地在这个项目上工作。)",
                "is", "are", "were", "have been",
                "A", "along with / together with / as well as 连接主语时，谓语动词与前面的主语一致。professor 是单数，用 is。");

        // --- 补充判断题 ---
        count += addTrueFalse(subjectId, 2,
                "\"There goes the bell.\" 这句话语法正确。", "T",
                "这是完全倒装句，here/there 置于句首且主语是名词时，句子完全倒装。如果主语是代词则不倒装：There it goes.");

        count += addTrueFalse(subjectId, 3,
                "\"I don't know what is his name.\" 这句话语法正确。", "F",
                "宾语从句必须用陈述语序，应改为 I don't know what his name is.");

        count += addTrueFalse(subjectId, 2,
                "\"Every boy and every girl has the right to education.\" 这句话语法正确。", "T",
                "every... and every... 连接主语时，谓语动词用单数。类似的还有 each... and each..., no... and no... 等。");

        // --- 补充填空题 ---
        count += addFillBlank(subjectId, 3,
                "If it ____ (rain) tomorrow, the sports meeting ____ (put) off.",
                "{\"1\":\"rains / should rain / were to rain\",\"2\":\"would be put / will be put\"}",
                "对将来情况的虚拟条件句，if 从句可用一般现在时/should+动词原形/were to+动词原形，主句用 would/should/could/might + 动词原形。此处运动会是被推迟，用被动。");

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

        // --- 补充选择题 ---
        count += addChoice(subjectId, 2,
                "函数 f(x) = ln(x² + 1) 的导数 f'(x) =",
                "2x/(x²+1)", "1/(x²+1)", "2x·ln(x²+1)", "(x²+1)/2x",
                "A", "复合函数求导：f'(x) = 1/(x²+1) · 2x = 2x/(x²+1)。");

        count += addChoice(subjectId, 3,
                "设 A 是 n 阶方阵，若 A² = A，则下列说法正确的是",
                "A 的特征值只能是 0 或 1", "A 一定是单位矩阵", "A 一定是零矩阵", "A 一定可逆",
                "A", "若 λ 是 A 的特征值，则 λ² = λ，解得 λ = 0 或 λ = 1。A²=A 称为幂等矩阵。");

        count += addChoice(subjectId, 2,
                "定积分 ∫(0 to π) sinx dx =",
                "2", "0", "1", "π",
                "A", "∫(0 to π) sinx dx = [-cosx](0 to π) = -cosπ - (-cos0) = -(-1) - (-1) = 2。");

        count += addChoice(subjectId, 4,
                "设 X 和 Y 相互独立，D(X)=4, D(Y)=9，则 D(2X-3Y) =",
                "97", "55", "25", "1",
                "A", "D(2X-3Y) = 4D(X) + 9D(Y) = 4×4 + 9×9 = 16 + 81 = 97。注意独立时方差相加，系数要平方。");

        count += addChoice(subjectId, 3,
                "微分方程 y' + y = 0 的通解为",
                "y = Ce⁻ˣ", "y = Ceˣ", "y = Cx", "y = C + e⁻ˣ",
                "A", "这是一阶线性齐次方程，分离变量 dy/y = -dx，积分得 lny = -x + C₁，即 y = Ce⁻ˣ。");

        count += addChoice(subjectId, 2,
                "向量 α=(1,2,3) 与 β=(4,5,6) 的点积 α·β =",
                "32", "21", "15", "42",
                "A", "α·β = 1×4 + 2×5 + 3×6 = 4 + 10 + 18 = 32。");

        // --- 补充判断题 ---
        count += addTrueFalse(subjectId, 2,
                "若函数 f(x) 在 [a,b] 上连续，则 f(x) 在 [a,b] 上一定有最大值和最小值。", "T",
                "这是闭区间上连续函数的最值定理（极值定理）。注意必须是闭区间且连续，开区间不一定成立。");

        count += addTrueFalse(subjectId, 3,
                "n 阶矩阵 A 有 n 个不同的特征值，则 A 一定可以对角化。", "T",
                "n 阶矩阵有 n 个不同特征值，则有 n 个线性无关的特征向量，故可对角化。但反过来不成立，有重特征值的矩阵也可能对角化。");

        count += addTrueFalse(subjectId, 2,
                "若 lim(n→∞) aₙ = 0，则级数 Σaₙ 一定收敛。", "F",
                "通项趋于零只是级数收敛的必要条件而非充分条件。反例：调和级数 Σ(1/n)，通项趋于零但级数发散。");

        // --- 补充填空题 ---
        count += addFillBlank(subjectId, 2,
                "lim(x→0) (eˣ - 1)/x = ____",
                "{\"1\":\"1\"}",
                "这是重要极限之一。可用洛必达法则：分子分母分别求导得 eˣ/1，令 x→0 得 1。也可用等价无穷小 eˣ-1 ~ x。");

        count += addFillBlank(subjectId, 3,
                "设 z = x²y + y³，则 ∂z/∂x = ____，∂z/∂y = ____",
                "{\"1\":\"2xy\",\"2\":\"x² + 3y²\"}",
                "偏导数：对 x 求偏导时把 y 当常数，∂z/∂x = 2xy；对 y 求偏导时把 x 当常数，∂z/∂y = x² + 3y²。");

        return count;
    }

    // ==================== 政治补充题 ====================

    private int loadPoliticsExtra(int subjectId) {
        int count = 0;
        count += addChoice(subjectId, 3, "构建人类命运共同体理念的核心是",
                "建设持久和平、普遍安全、共同繁荣、开放包容、清洁美丽的世界",
                "实现全球经济一体化", "建立国际军事同盟", "推行西方民主制度",
                "A", "习近平提出的人类命运共同体理念，核心是建设五个世界。");
        count += addChoice(subjectId, 2, "中国共产党人的初心和使命是",
                "为中国人民谋幸福，为中华民族谋复兴", "实现共产主义", "全面建成小康社会", "实现中华民族伟大复兴的中国梦",
                "A", "党的十九大报告指出，中国共产党人的初心和使命就是为中国人民谋幸福，为中华民族谋复兴。");
        count += addTrueFalse(subjectId, 2, "量变是质变的必要准备，质变是量变的必然结果。", "T",
                "这是量变质变规律的基本内容。量变积累到一定程度必然引起质变，质变又为新的量变开辟道路。");
        count += addTrueFalse(subjectId, 3, "经济基础决定上层建筑，上层建筑反作用于经济基础。", "T",
                "这是历史唯物主义的基本原理。上层建筑的反作用可以是促进也可以是阻碍，取决于它是否适应经济基础。");
        count += addFillBlank(subjectId, 2, "四个自信是指____、____、____和____。",
                "{\"1\":\"道路自信\",\"2\":\"理论自信\",\"3\":\"制度自信\",\"4\":\"文化自信\"}",
                "习近平总书记提出的四个自信，是对中国特色社会主义的信念。");
        count += addChoice(subjectId, 2, "马克思主义中国化的最新成果是",
                "习近平新时代中国特色社会主义思想", "邓小平理论", "三个代表重要思想", "科学发展观",
                "A", "习近平新时代中国特色社会主义思想是马克思主义中国化最新成果，是党和人民实践经验和集体智慧的结晶。");
        count += addChoice(subjectId, 3, "资本积累的实质是",
                "资本家把剩余价值的一部分转化为资本", "资本家节约消费", "技术进步", "扩大市场需求",
                "A", "资本积累就是把剩余价值再转化为资本，即剩余价值的资本化。");
        return count;
    }

    // ==================== 英语补充题 ====================

    private int loadEnglishExtra(int subjectId) {
        int count = 0;
        count += addChoice(subjectId, 2, "By the time you get back, I ____ finished all the work.\n(等你回来的时候，我就已经完成所有工作了。)",
                "will have", "have", "had", "will",
                "A", "By the time + 一般现在时，主句用将来完成时 will have done，表示在将来某时刻之前已完成的动作。");
        count += addChoice(subjectId, 3, "The reason ____ he gave for his absence is ____ he was ill.\n(他给出的缺席理由是他生病了。)",
                "that; that", "why; that", "that; because", "why; because",
                "A", "第一个空是定语从句修饰reason，在从句中作gave的宾语，用that；第二个空是表语从句，用that引导。注意the reason why...is that...和the reason that...is that...的区别。");
        count += addTrueFalse(subjectId, 2, "\"She is one of those people who always keeps their promises.\" 这句话语法正确。", "F",
                "who引导的定语从句修饰those people（复数），谓语应该用keep而不是keeps。应改为who always keep their promises。");
        count += addChoice(subjectId, 2, "I'll never forget the day ____ I joined the Party.\n(我永远不会忘记我入党的那一天。)",
                "when", "which", "that", "where",
                "A", "先行词the day在定语从句中作时间状语，用关系副词when引导。");
        count += addFillBlank(subjectId, 2, "It is essential that every student ____ (attend) the meeting.",
                "{\"1\":\"(should) attend\"}",
                "It is essential/important/necessary that... 后面的从句用虚拟语气 (should) + 动词原形。");
        count += addChoice(subjectId, 3, "So absorbed ____ in her work that she didn't notice it was time to go home.\n(她太专注于工作了，以至于没注意到该回家了。)",
                "she was", "was she", "she had been", "had she been",
                "B", "So + adj. 置于句首时，主句需要部分倒装。So absorbed was she... = She was so absorbed...");
        count += addTrueFalse(subjectId, 2, "\"Neither you nor I am wrong.\" 这句话语法正确。", "T",
                "Neither...nor...连接主语时遵循就近原则，I离谓语最近，所以用am。");
        return count;
    }

    // ==================== 数学补充题 ====================

    private int loadMathExtra(int subjectId) {
        int count = 0;
        count += addChoice(subjectId, 2, "设 f(x) = x³ - 6x² + 9x + 1，则 f(x) 的极大值为",
                "5", "1", "3", "7",
                "A", "f'(x) = 3x²-12x+9 = 3(x-1)(x-3)，令f'(x)=0得x=1,x=3。f''(1)=-6<0故x=1是极大值点，f(1)=1-6+9+1=5。");
        count += addChoice(subjectId, 3, "设 A 为 3×3 矩阵，r(A)=2，则齐次方程组 Ax=0 的基础解系含有几个向量",
                "1", "2", "3", "0",
                "A", "基础解系含 n-r(A) = 3-2 = 1 个线性无关的解向量。");
        count += addTrueFalse(subjectId, 2, "若 f(x) 在 x₀ 处取得极值且 f'(x₀) 存在，则 f'(x₀) = 0。", "T",
                "这是费马定理。可导函数在极值点处的导数为零。但反过来不成立，f'(x₀)=0不一定是极值点。");
        count += addFillBlank(subjectId, 2, "∫(0 to 1) x·eˣ dx = ____",
                "{\"1\":\"1\"}",
                "分部积分：∫xeˣdx = xeˣ - ∫eˣdx = xeˣ - eˣ + C = eˣ(x-1) + C。代入上下限：e(1-1) - e⁰(0-1) = 0-(-1) = 1。");
        count += addChoice(subjectId, 3, "随机变量 X~B(10, 0.3)，则 E(X) 和 D(X) 分别为",
                "3 和 2.1", "3 和 0.9", "7 和 2.1", "0.3 和 0.21",
                "A", "二项分布 B(n,p)：E(X)=np=10×0.3=3，D(X)=np(1-p)=10×0.3×0.7=2.1。");
        count += addTrueFalse(subjectId, 3, "若级数 Σ|aₙ| 收敛，则级数 Σaₙ 一定收敛。", "T",
                "绝对收敛必收敛。这是级数理论的基本定理。反过来不成立，条件收敛的级数 Σaₙ 收敛但 Σ|aₙ| 发散。");
        count += addChoice(subjectId, 2, "曲线 y = x² 在点 (1,1) 处的切线方程为",
                "y = 2x - 1", "y = 2x + 1", "y = x - 1", "y = x + 1",
                "A", "y' = 2x，在x=1处斜率为2。切线方程：y-1 = 2(x-1)，即 y = 2x-1。");
        count += addChoice(subjectId, 3, "设随机变量 X 的概率密度为 f(x)=2x (0<x<1)，则 P(X>0.5) =",
                "0.75", "0.5", "0.25", "1",
                "A", "P(X>0.5) = ∫(0.5 to 1) 2x dx = [x²](0.5 to 1) = 1 - 0.25 = 0.75。");
        count += addTrueFalse(subjectId, 2, "矩阵的行列式等于其转置矩阵的行列式。", "T",
                "|A| = |Aᵀ|，这是行列式的基本性质。");
        count += addFillBlank(subjectId, 3, "微分方程 y'' - 4y = 0 的通解为 y = ____",
                "{\"1\":\"C₁e²ˣ + C₂e⁻²ˣ\"}",
                "特征方程 r²-4=0，r=±2，通解 y=C₁e²ˣ+C₂e⁻²ˣ。");
        count += addChoice(subjectId, 2, "∫sin²x dx =",
                "x/2 - sin(2x)/4 + C", "-cos²x + C", "sin²x/2 + C", "-cos(2x)/2 + C",
                "A", "用半角公式 sin²x = (1-cos2x)/2，积分得 x/2 - sin(2x)/4 + C。");
        count += addTrueFalse(subjectId, 3, "若 A 和 B 都是 n 阶可逆矩阵，则 (AB)⁻¹ = B⁻¹A⁻¹。", "T",
                "矩阵乘积的逆等于各矩阵逆的反序乘积。注意顺序不能颠倒，因为矩阵乘法不满足交换律。");
        count += addChoice(subjectId, 4, "设 X₁,X₂,...,Xₙ 是来自总体 N(μ,σ²) 的简单随机样本，则样本均值 X̄ 服从",
                "N(μ, σ²/n)", "N(μ, σ²)", "N(nμ, σ²)", "N(μ, nσ²)",
                "A", "样本均值的期望 E(X̄)=μ，方差 D(X̄)=σ²/n，故 X̄~N(μ,σ²/n)。");
        return count;
    }

    // ==================== 专业课题目（计算机408风格） ====================

    private int loadMajorQuestions(int subjectId) {
        int count = 0;

        // 数据结构
        count += addChoice(subjectId, 2, "一棵完全二叉树有 100 个结点，则叶子结点的个数为",
                "50", "51", "49", "100",
                "A", "完全二叉树中，n₀ = n₂ + 1。100个结点中 n₁=0或1，这里 n=100 为偶数所以 n₁=1，n₀+n₁+n₂=100，n₀=n₂+1，解得 n₀=50。");

        count += addChoice(subjectId, 3, "对 n 个元素进行快速排序，最坏情况下的时间复杂度为",
                "O(n²)", "O(nlogn)", "O(n)", "O(logn)",
                "A", "快排平均 O(nlogn)，但最坏情况（每次划分极不平衡，如已有序数组）退化为 O(n²)。");

        count += addChoice(subjectId, 2, "在一个长度为 n 的顺序表中，插入一个元素的平均移动次数为",
                "n/2", "n", "n-1", "(n+1)/2",
                "A", "在位置 i 插入需移动 n-i 个元素，i 从 0 到 n 等概率，平均移动 n/2 次。");

        count += addTrueFalse(subjectId, 2, "栈和队列都是操作受限的线性表。", "T",
                "栈只允许在一端（栈顶）进行插入和删除，队列只允许在一端插入另一端删除，都是操作受限的线性表。");

        count += addTrueFalse(subjectId, 3, "哈希表的查找时间复杂度一定是 O(1)。", "F",
                "哈希表在理想情况下（无冲突或冲突很少）查找为 O(1)，但最坏情况下（所有元素都冲突到同一个位置）退化为 O(n)。");

        // 操作系统
        count += addChoice(subjectId, 2, "进程和线程的主要区别是",
                "进程是资源分配的单位，线程是调度的单位", "进程比线程执行速度快", "线程不能共享内存", "一个进程只能有一个线程",
                "A", "进程是操作系统资源分配的基本单位，线程是CPU调度的基本单位。同一进程的线程共享地址空间。");

        count += addChoice(subjectId, 3, "下列页面置换算法中，可能产生 Belady 异常的是",
                "FIFO", "LRU", "OPT", "LFU",
                "A", "Belady 异常是指增加物理页框数反而导致缺页率上升。只有 FIFO 算法可能出现这种现象，LRU 和 OPT 属于栈算法不会出现。");

        count += addTrueFalse(subjectId, 2, "死锁的四个必要条件是互斥、占有并等待、不可剥夺和循环等待。", "T",
                "这四个条件必须同时满足才会发生死锁，破坏其中任何一个就可以预防死锁。");

        // 计算机网络
        count += addChoice(subjectId, 2, "TCP 三次握手的目的是",
                "确认双方的发送和接收能力都正常", "加密通信数据", "分配 IP 地址", "建立路由表",
                "A", "三次握手确保双方都知道对方能发也能收：第一次确认客户端发送正常，第二次确认服务端接收和发送正常，第三次确认客户端接收正常。");

        count += addChoice(subjectId, 3, "HTTP 协议默认使用的端口号是",
                "80", "443", "21", "25",
                "A", "HTTP 默认端口 80，HTTPS 默认端口 443，FTP 默认端口 21，SMTP 默认端口 25。");

        count += addTrueFalse(subjectId, 2, "IP 协议提供的是可靠的、面向连接的数据传输服务。", "F",
                "IP 协议提供的是不可靠的、无连接的数据报传输服务。可靠性由上层的 TCP 协议来保证。");

        // 计算机组成原理
        count += addChoice(subjectId, 2, "Cache 的地址映射方式中，冲突概率最低的是",
                "全相联映射", "直接映射", "组相联映射", "随机映射",
                "A", "全相联映射允许主存块放到 Cache 的任意位置，冲突概率最低但硬件成本高。直接映射冲突最多，组相联是折中方案。");

        count += addChoice(subjectId, 3, "某计算机字长 32 位，按字节编址，主存容量 4GB，则主存地址寄存器至少需要多少位",
                "32", "16", "64", "8",
                "A", "4GB = 2³² 字节，按字节编址需要 32 位地址线来寻址。");

        count += addFillBlank(subjectId, 2, "OSI 七层模型从下到上依次是物理层、数据链路层、____、传输层、会话层、表示层和____。",
                "{\"1\":\"网络层\",\"2\":\"应用层\"}",
                "OSI 七层：物理层→数据链路层→网络层→传输层→会话层→表示层→应用层。记忆口诀：物数网传会表应。");

        count += addTrueFalse(subjectId, 3, "虚拟存储器的最大容量由计算机的地址结构决定，与实际内存大小无关。", "T",
                "虚拟存储器的最大容量取决于 CPU 的地址位数（如 32 位系统最大 4GB），实际容量取虚存最大容量与内存+外存之和的较小值。");

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
