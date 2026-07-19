package com.example.clubmanagementsystem.config;

import com.example.clubmanagementsystem.entity.*;
import com.example.clubmanagementsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ClubInfoService clubInfoService;

    @Autowired
    private ClubMemberService clubMemberService;

    @Autowired
    private ActivityInfoService activityInfoService;

    @Autowired
    private ActivityRegistrationService activityRegistrationService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        initAdminAccount();
        initTestData();
    }

    private void initAdminAccount() {
        SysUser admin = sysUserService.getById("admin");
        if (admin == null) {
            admin = new SysUser();
            admin.setStudentId("admin");
            admin.setName("系统管理员");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(1);
            admin.setCreateTime(LocalDateTime.now());
            sysUserService.save(admin);
            System.out.println("[DataInit] 管理员账号已创建: admin / admin123");
        } else if (!passwordEncoder.matches("admin123", admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode("admin123"));
            sysUserService.updateById(admin);
            System.out.println("[DataInit] 管理员密码已修复: admin / admin123");
        }
    }

    private void initTestData() {
        if (sysUserService.getById("2024001") != null) {
            return;
        }

        System.out.println("[DataInit] 开始初始化测试数据...");

        // ===== 15个普通用户 =====
        saveUser("2024001", "张三", "计科2401", "13800000001");
        saveUser("2024002", "李四", "软工2401", "13800000002");
        saveUser("2024003", "王五", "计科2402", "13800000003");
        saveUser("2024004", "赵六", "软工2402", "13800000004");
        saveUser("2024005", "孙七", "计科2403", "13800000005");
        saveUser("2024006", "周八", "数媒2401", "13800000006");
        saveUser("2024007", "吴九", "信安2401", "13800000007");
        saveUser("2024008", "郑十", "计科2401", "13800000008");
        saveUser("2024009", "冯雪", "软工2401", "13800000009");
        saveUser("2024010", "陈诚", "计科2402", "13800000010");
        saveUser("2024011", "褚明", "数媒2401", "13800000011");
        saveUser("2024012", "卫兰", "信安2401", "13800000012");
        saveUser("2024013", "蒋磊", "计科2403", "13800000013");
        saveUser("2024014", "沈洋", "软工2402", "13800000014");
        saveUser("2024015", "韩梅", "计科2401", "13800000015");

        // ===== 10个社团 =====
        ClubInfo club1 = saveClub("编程爱好者协会", "学术科技", LocalDate.of(2023, 9, 1), "刘建国", "2024001",
                "热爱编程的同学们的家园，定期举办编程竞赛、技术分享会和Hackathon活动");
        ClubInfo club2 = saveClub("篮球社", "体育运动", LocalDate.of(2022, 3, 15), "陈志强", "2024002",
                "以球会友、强身健体，每周定期训练，参加校际联赛");
        ClubInfo club3 = saveClub("吉他音乐社", "文化艺术", LocalDate.of(2023, 3, 10), "王艺萌", "2024003",
                "零基础入门到进阶弹唱，每学期举办校园音乐会和弹唱之夜");
        ClubInfo club4 = saveClub("摄影协会", "文化艺术", LocalDate.of(2021, 10, 20), "张晓光", "2024004",
                "记录校园生活之美，定期组织外拍活动和摄影展览");
        ClubInfo club5 = saveClub("机器人创新实验室", "学术科技", LocalDate.of(2022, 9, 5), "赵明远", "2024005",
                "专注机器人设计与竞赛，备战RoboMaster等全国性比赛");
        ClubInfo club6 = saveClub("志愿服务队", "公益实践", LocalDate.of(2020, 5, 4), "李思远", "2024006",
                "奉献爱心、服务社会，组织社区帮扶、支教和环保志愿活动");
        ClubInfo club7 = saveClub("辩论社", "人文社科", LocalDate.of(2021, 4, 12), "郑海峰", "2024007",
                "以辩会友、思辨天下，培养逻辑思维和语言表达能力");
        ClubInfo club8 = saveClub("动漫社", "文化艺术", LocalDate.of(2022, 10, 1), "吴晓燕", "2024008",
                "ACGN文化交流，举办cosplay展览、动漫鉴赏会和同人创作");
        ClubInfo club9 = saveClub("跑步俱乐部", "体育运动", LocalDate.of(2023, 4, 20), "孙晓峰", "2024009",
                "健康跑步、快乐生活，组织晨跑团和校园马拉松活动");
        ClubInfo club10 = saveClub("创业孵化社", "学术科技", LocalDate.of(2023, 6, 1), "周晓东", "2024010",
                "汇聚创意、孵化梦想，组织创业讲座和项目路演活动");
        ClubInfo club11 = saveClub("书法社", "文化艺术", LocalDate.of(2021, 9, 15), "林墨涵", "2024011",
                "传承书法艺术，定期开展硬笔和毛笔书法教学，举办书法展览", 30);
        ClubInfo club12 = saveClub("街舞社", "体育运动", LocalDate.of(2022, 11, 8), "黄子韬", "2024012",
                "Hiphop、Breaking、Popping多舞种教学，每年参加大学生街舞大赛", 25);
        ClubInfo club13 = saveClub("数学建模协会", "学术科技", LocalDate.of(2020, 3, 1), "钱学林", "2024013",
                "备战全国数学建模竞赛，每周集训MATLAB编程与论文写作", 35);
        ClubInfo club14 = saveClub("英语角", "人文社科", LocalDate.of(2022, 4, 10), "Sarah Wang", "2024014",
                "每周英语口语练习，外教定期参与，举办英语演讲比赛和英语戏剧", 40);
        ClubInfo club15 = saveClub("电影鉴赏社", "文化艺术", LocalDate.of(2023, 10, 1), "冯小刚", "2024015",
                "每周观影+讨论，涵盖经典电影、独立电影和纪录片，培养影视审美", 30);
        ClubInfo club16 = saveClub("动画制作社", "文化艺术", LocalDate.of(2023, 5, 20), "宫崎骏", "2024011",
                "学习2D/3D动画制作，使用Blender和AE完成原创短片，参加大学生动画展映");
        ClubInfo club17 = saveClub("电子竞技社", "体育运动", LocalDate.of(2022, 6, 18), "李晓峰", "2024008",
                "英雄联盟、VALORANT等项目训练与比赛，代表学校参加CUEA高校联赛");
        ClubInfo club18 = saveClub("天文观测社", "学术科技", LocalDate.of(2021, 11, 5), "张衡宇", "2024013",
                "定期组织户外观星活动，学习天文知识，使用望远镜观测行星和深空天体");
        ClubInfo club19 = saveClub("烘焙美食社", "生活休闲", LocalDate.of(2023, 9, 10), "陈美琪", "2024009",
                "每周一次烘焙实操课，从蛋糕、面包到甜点，享受动手制作的乐趣");
        ClubInfo club20 = saveClub("算法竞赛社", "学术科技", LocalDate.of(2021, 3, 15), "陈立杰", "2024001",
                "备战ACM-ICPC、蓝桥杯等程序设计竞赛，每周3次集训，暑期集中训练营");

        // ===== 社团成员关系 =====
        // 编程协会（8人）
        saveMember(club1.getClubId(), "2024001", "社长");
        saveMember(club1.getClubId(), "2024002", "副社长");
        saveMember(club1.getClubId(), "2024005", "技术部长");
        saveMember(club1.getClubId(), "2024007", "成员");
        saveMember(club1.getClubId(), "2024008", "成员");
        saveMember(club1.getClubId(), "2024010", "成员");
        saveMember(club1.getClubId(), "2024013", "成员");
        saveMember(club1.getClubId(), "2024015", "成员");

        // 篮球社（6人）
        saveMember(club2.getClubId(), "2024002", "社长");
        saveMember(club2.getClubId(), "2024003", "副社长");
        saveMember(club2.getClubId(), "2024004", "成员");
        saveMember(club2.getClubId(), "2024006", "成员");
        saveMember(club2.getClubId(), "2024010", "成员");
        saveMember(club2.getClubId(), "2024014", "成员");

        // 吉他音乐社（6人）
        saveMember(club3.getClubId(), "2024003", "社长");
        saveMember(club3.getClubId(), "2024006", "副社长");
        saveMember(club3.getClubId(), "2024001", "成员");
        saveMember(club3.getClubId(), "2024008", "成员");
        saveMember(club3.getClubId(), "2024011", "成员");
        saveMember(club3.getClubId(), "2024015", "成员");

        // 摄影协会（5人）
        saveMember(club4.getClubId(), "2024004", "社长");
        saveMember(club4.getClubId(), "2024005", "成员");
        saveMember(club4.getClubId(), "2024006", "成员");
        saveMember(club4.getClubId(), "2024011", "成员");
        saveMember(club4.getClubId(), "2024012", "成员");

        // 机器人实验室（6人）
        saveMember(club5.getClubId(), "2024005", "社长");
        saveMember(club5.getClubId(), "2024007", "副社长");
        saveMember(club5.getClubId(), "2024001", "成员");
        saveMember(club5.getClubId(), "2024008", "成员");
        saveMember(club5.getClubId(), "2024013", "成员");
        saveMember(club5.getClubId(), "2024014", "成员");

        // 志愿服务队（7人）
        saveMember(club6.getClubId(), "2024006", "队长");
        saveMember(club6.getClubId(), "2024002", "成员");
        saveMember(club6.getClubId(), "2024004", "成员");
        saveMember(club6.getClubId(), "2024007", "成员");
        saveMember(club6.getClubId(), "2024009", "成员");
        saveMember(club6.getClubId(), "2024012", "成员");
        saveMember(club6.getClubId(), "2024015", "成员");

        // 辩论社（5人）
        saveMember(club7.getClubId(), "2024007", "社长");
        saveMember(club7.getClubId(), "2024009", "副社长");
        saveMember(club7.getClubId(), "2024003", "成员");
        saveMember(club7.getClubId(), "2024012", "成员");
        saveMember(club7.getClubId(), "2024014", "成员");

        // 动漫社（5人）
        saveMember(club8.getClubId(), "2024008", "社长");
        saveMember(club8.getClubId(), "2024011", "副社长");
        saveMember(club8.getClubId(), "2024003", "成员");
        saveMember(club8.getClubId(), "2024013", "成员");
        saveMember(club8.getClubId(), "2024015", "成员");

        // 跑步俱乐部（5人）
        saveMember(club9.getClubId(), "2024009", "社长");
        saveMember(club9.getClubId(), "2024010", "成员");
        saveMember(club9.getClubId(), "2024002", "成员");
        saveMember(club9.getClubId(), "2024014", "成员");
        saveMember(club9.getClubId(), "2024006", "成员");

        // 创业孵化社（4人）
        saveMember(club10.getClubId(), "2024010", "社长");
        saveMember(club10.getClubId(), "2024001", "成员");
        saveMember(club10.getClubId(), "2024005", "成员");
        saveMember(club10.getClubId(), "2024009", "成员");

        // ===== 活动数据（共15个活动） =====

        // --- 报名中的活动（status=1）---
        ActivityInfo a1 = saveActivity(club1.getClubId(), "2024001", "Spring Boot 技术分享会",
                "邀请高年级学长分享Spring Boot微服务开发经验，包含实战项目演示和代码走读环节",
                LocalDateTime.now().plusDays(7), "图书馆报告厅", 50, 1);

        ActivityInfo a2 = saveActivity(club2.getClubId(), "2024002", "新生篮球友谊赛",
                "欢迎所有2024级新生参加，分队进行友谊比赛，赛后有自由练习时间",
                LocalDateTime.now().plusDays(3), "体育馆A区", 30, 1);

        ActivityInfo a3 = saveActivity(club3.getClubId(), "2024003", "校园民谣弹唱之夜",
                "社团成员轮流上台表演，观众可点歌互动，提供茶歇小食",
                LocalDateTime.now().plusDays(5), "学生活动中心", 80, 1);

        ActivityInfo a4 = saveActivity(club5.getClubId(), "2024005", "RoboMaster 校内选拔赛",
                "选拔参加全国大学生机器人大赛的队员，要求有编程或机械基础，带笔记本电脑",
                LocalDateTime.now().plusDays(10), "工科楼实验室", 20, 1);

        ActivityInfo a5 = saveActivity(club6.getClubId(), "2024006", "社区敬老院志愿服务",
                "前往校外社区敬老院陪伴老人聊天、整理房间，传递温暖",
                LocalDateTime.now().plusDays(4), "校门口集合出发", 15, 1);

        ActivityInfo a6 = saveActivity(club7.getClubId(), "2024007", "新生杯辩论赛",
                "辩题：人工智能发展利大于弊vs弊大于利，报名后随机分组",
                LocalDateTime.now().plusDays(6), "教学楼C201", 24, 1);

        ActivityInfo a7 = saveActivity(club9.getClubId(), "2024009", "环校园夜跑活动",
                "绕校园主干道慢跑5公里，配速自由，终点有补给站和合影",
                LocalDateTime.now().plusDays(2), "操场集合", 60, 1);

        ActivityInfo a8 = saveActivity(club10.getClubId(), "2024010", "创业项目路演大赛",
                "6支学生团队展示创业计划，邀请创投导师点评指导",
                LocalDateTime.now().plusDays(12), "创新创业中心", 40, 1);

        // --- 待审批的活动（status=0）---
        ActivityInfo a9 = saveActivity(club4.getClubId(), "2024004", "秋季校园摄影大赛",
                "以'校园秋色'为主题，征集摄影作品，优秀作品在校展览中心展出一周",
                LocalDateTime.now().plusDays(14), "全校范围", 100, 0);

        ActivityInfo a10 = saveActivity(club1.getClubId(), "2024001", "ACM算法集训营",
                "为期两周的算法强化训练，每天2小时刷题+讲解，备战省级程序设计竞赛",
                LocalDateTime.now().plusDays(20), "计算机学院机房", 40, 0);

        ActivityInfo a11 = saveActivity(club8.getClubId(), "2024008", "校园Cosplay嘉年华",
                "动漫角色Cosplay展示、同人创作展览、二次元知识竞赛，欢迎ACGN爱好者",
                LocalDateTime.now().plusDays(18), "学生活动中心广场", 120, 0);

        // --- 已结束的活动（status=2）---
        ActivityInfo a12 = saveActivity(club2.getClubId(), "2024002", "院际篮球联赛决赛",
                "计算机学院vs信息学院的篮球对抗决赛，观众免费入场",
                LocalDateTime.now().minusDays(5), "体育馆主场", 24, 2);

        ActivityInfo a13 = saveActivity(club3.getClubId(), "2024003", "新生吉他入门体验课",
                "面向零基础新生的吉他免费体验活动，提供教学吉他，手把手教学基础和弦",
                LocalDateTime.now().minusDays(10), "音乐教室B102", 20, 2);

        ActivityInfo a14 = saveActivity(club6.getClubId(), "2024006", "校园环保清洁行动",
                "清理校园角落垃圾、维护花坛绿植，共建美丽校园环境",
                LocalDateTime.now().minusDays(3), "校园各区域", 30, 2);

        ActivityInfo a15 = saveActivity(club5.getClubId(), "2024005", "Arduino入门工作坊",
                "手把手教学Arduino单片机基础，完成LED闪烁和传感器读取小项目",
                LocalDateTime.now().minusDays(7), "工科楼3楼实验室", 25, 2);

        // ===== 报名签到数据 =====

        // 活动1 - Spring Boot分享会 (6人报名)
        saveRegistration(a1.getActivityId(), "2024002", 0, null);
        saveRegistration(a1.getActivityId(), "2024003", 0, null);
        saveRegistration(a1.getActivityId(), "2024005", 0, null);
        saveRegistration(a1.getActivityId(), "2024007", 0, null);
        saveRegistration(a1.getActivityId(), "2024010", 0, null);
        saveRegistration(a1.getActivityId(), "2024013", 0, null);

        // 活动2 - 新生篮球友谊赛 (7人报名)
        saveRegistration(a2.getActivityId(), "2024001", 0, null);
        saveRegistration(a2.getActivityId(), "2024003", 0, null);
        saveRegistration(a2.getActivityId(), "2024004", 0, null);
        saveRegistration(a2.getActivityId(), "2024006", 0, null);
        saveRegistration(a2.getActivityId(), "2024008", 0, null);
        saveRegistration(a2.getActivityId(), "2024010", 0, null);
        saveRegistration(a2.getActivityId(), "2024014", 0, null);

        // 活动3 - 弹唱之夜 (5人报名)
        saveRegistration(a3.getActivityId(), "2024001", 0, null);
        saveRegistration(a3.getActivityId(), "2024006", 0, null);
        saveRegistration(a3.getActivityId(), "2024008", 0, null);
        saveRegistration(a3.getActivityId(), "2024011", 0, null);
        saveRegistration(a3.getActivityId(), "2024015", 0, null);

        // 活动5 - 敬老院志愿 (4人报名)
        saveRegistration(a5.getActivityId(), "2024002", 0, null);
        saveRegistration(a5.getActivityId(), "2024004", 0, null);
        saveRegistration(a5.getActivityId(), "2024009", 0, null);
        saveRegistration(a5.getActivityId(), "2024012", 0, null);

        // 活动6 - 辩论赛 (6人报名)
        saveRegistration(a6.getActivityId(), "2024003", 0, null);
        saveRegistration(a6.getActivityId(), "2024009", 0, null);
        saveRegistration(a6.getActivityId(), "2024012", 0, null);
        saveRegistration(a6.getActivityId(), "2024014", 0, null);
        saveRegistration(a6.getActivityId(), "2024001", 0, null);
        saveRegistration(a6.getActivityId(), "2024005", 0, null);

        // 活动7 - 夜跑 (8人报名)
        saveRegistration(a7.getActivityId(), "2024002", 0, null);
        saveRegistration(a7.getActivityId(), "2024006", 0, null);
        saveRegistration(a7.getActivityId(), "2024009", 0, null);
        saveRegistration(a7.getActivityId(), "2024010", 0, null);
        saveRegistration(a7.getActivityId(), "2024014", 0, null);
        saveRegistration(a7.getActivityId(), "2024001", 0, null);
        saveRegistration(a7.getActivityId(), "2024011", 0, null);
        saveRegistration(a7.getActivityId(), "2024015", 0, null);

        // 活动8 - 创业路演 (3人报名)
        saveRegistration(a8.getActivityId(), "2024001", 0, null);
        saveRegistration(a8.getActivityId(), "2024005", 0, null);
        saveRegistration(a8.getActivityId(), "2024009", 0, null);

        // 已结束活动12 - 院际篮球决赛 (8人报名，6人签到)
        saveRegistration(a12.getActivityId(), "2024001", 1, LocalDateTime.now().minusDays(5));
        saveRegistration(a12.getActivityId(), "2024003", 1, LocalDateTime.now().minusDays(5));
        saveRegistration(a12.getActivityId(), "2024004", 1, LocalDateTime.now().minusDays(5));
        saveRegistration(a12.getActivityId(), "2024006", 1, LocalDateTime.now().minusDays(5));
        saveRegistration(a12.getActivityId(), "2024010", 1, LocalDateTime.now().minusDays(5));
        saveRegistration(a12.getActivityId(), "2024014", 1, LocalDateTime.now().minusDays(5));
        saveRegistration(a12.getActivityId(), "2024008", 0, null);
        saveRegistration(a12.getActivityId(), "2024011", 0, null);

        // 已结束活动13 - 吉他体验课 (6人报名，5人签到)
        saveRegistration(a13.getActivityId(), "2024001", 1, LocalDateTime.now().minusDays(10));
        saveRegistration(a13.getActivityId(), "2024005", 1, LocalDateTime.now().minusDays(10));
        saveRegistration(a13.getActivityId(), "2024006", 1, LocalDateTime.now().minusDays(10));
        saveRegistration(a13.getActivityId(), "2024011", 1, LocalDateTime.now().minusDays(10));
        saveRegistration(a13.getActivityId(), "2024015", 1, LocalDateTime.now().minusDays(10));
        saveRegistration(a13.getActivityId(), "2024008", 0, null);

        // 已结束活动14 - 环保清洁 (7人报名，全部签到)
        saveRegistration(a14.getActivityId(), "2024002", 1, LocalDateTime.now().minusDays(3));
        saveRegistration(a14.getActivityId(), "2024004", 1, LocalDateTime.now().minusDays(3));
        saveRegistration(a14.getActivityId(), "2024006", 1, LocalDateTime.now().minusDays(3));
        saveRegistration(a14.getActivityId(), "2024009", 1, LocalDateTime.now().minusDays(3));
        saveRegistration(a14.getActivityId(), "2024012", 1, LocalDateTime.now().minusDays(3));
        saveRegistration(a14.getActivityId(), "2024013", 1, LocalDateTime.now().minusDays(3));
        saveRegistration(a14.getActivityId(), "2024015", 1, LocalDateTime.now().minusDays(3));

        // 已结束活动15 - Arduino工作坊 (5人报名，4人签到)
        saveRegistration(a15.getActivityId(), "2024005", 1, LocalDateTime.now().minusDays(7));
        saveRegistration(a15.getActivityId(), "2024007", 1, LocalDateTime.now().minusDays(7));
        saveRegistration(a15.getActivityId(), "2024008", 1, LocalDateTime.now().minusDays(7));
        saveRegistration(a15.getActivityId(), "2024013", 1, LocalDateTime.now().minusDays(7));
        saveRegistration(a15.getActivityId(), "2024001", 0, null);

        System.out.println("[DataInit] =============================================");
        System.out.println("[DataInit] 测试数据初始化完成！");
        System.out.println("[DataInit] 用户: 15人 (2024001~2024015，密码: 123456)");
        System.out.println("[DataInit] 社团: 20个");
        System.out.println("[DataInit] 活动: 15个 (8个报名中 + 3个待审批 + 4个已结束)");
        System.out.println("[DataInit] =============================================");
    }

    // ===== 辅助方法 =====

    private void saveUser(String studentId, String name, String className, String contact) {
        SysUser user = new SysUser();
        user.setStudentId(studentId);
        user.setName(name);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setClassName(className);
        user.setRole(0);
        user.setContactInfo(contact);
        user.setCreateTime(LocalDateTime.now());
        sysUserService.save(user);
    }

    private ClubInfo saveClub(String name, String category, LocalDate date, String advisor, String creatorId, String desc) {
        return saveClub(name, category, date, advisor, creatorId, desc, 200);
    }

    private ClubInfo saveClub(String name, String category, LocalDate date, String advisor, String creatorId, String desc, int maxMembers) {
        ClubInfo club = new ClubInfo();
        club.setName(name);
        club.setCategory(category);
        club.setEstablishedDate(date);
        club.setAdvisor(advisor);
        club.setCreatorId(creatorId);
        club.setDescription(desc);
        club.setMaxMembers(maxMembers);
        club.setStatus(1); // 正常状态
        club.setIsDeleted(0);
        clubInfoService.save(club);
        return club;
    }

    private void saveMember(Integer clubId, String studentId, String position) {
        ClubMember member = new ClubMember();
        member.setClubId(clubId);
        member.setStudentId(studentId);
        member.setPosition(position);
        member.setStatus(1);
        member.setJoinTime(LocalDateTime.now());
        clubMemberService.save(member);
    }

    private ActivityInfo saveActivity(Integer clubId, String applicantId, String title, String details,
                                      LocalDateTime eventTime, String location, int maxParticipants, int status) {
        ActivityInfo activity = new ActivityInfo();
        activity.setClubId(clubId);
        activity.setApplicantId(applicantId);
        activity.setTitle(title);
        activity.setDetails(details);
        activity.setEventTime(eventTime);
        activity.setLocation(location);
        activity.setMaxParticipants(maxParticipants);
        activity.setStatus(status);
        activity.setIsDeleted(0);
        activityInfoService.save(activity);
        return activity;
    }

    private void saveRegistration(Integer activityId, String studentId, int signInStatus, LocalDateTime signInTime) {
        ActivityRegistration reg = new ActivityRegistration();
        reg.setActivityId(activityId);
        reg.setStudentId(studentId);
        reg.setRegTime(LocalDateTime.now());
        reg.setSignInStatus(signInStatus);
        if (signInTime != null) {
            reg.setSignInTime(signInTime);
        }
        activityRegistrationService.save(reg);
    }
}
