package homework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.io.*;


public class MUD {
    public static void main(String[] args) {
        GameManager gameManager = new GameManager();
        gameManager.startGame();
    }
}

//技能类
class Skill implements Serializable{
    private String name;
    private String description;
    private String type;
    private int basePower;
    private int level;
    private int unlockLevel;
    private boolean onCooldown;
    private int coolDownRemaining;
    private static final long serialVersionUID = 1L;

    public Skill(String name, String description, String type, int basePower, int unlockLevel){
        this.name = name;
        this.description = description;
        this.type = type;
        this.basePower = basePower;
        this.level = 1;
        this.unlockLevel = unlockLevel;
        this.onCooldown = false;
        this.coolDownRemaining = 0;
    }
    //计算技能理论效果
    public double calculateEffect(){
        return (double)(basePower * (1+0.2*level));
    }
    //计算技能实际效果（随即偏差）
    public double getActualValue() {
        double baseEffect = calculateEffect();
        double randomFactor = 0.8 + Math.random() * 0.4;
        return (double)(baseEffect * randomFactor);
    }
    //技能升级
    public void levelUp() {
        this.level++;
    }

    public boolean canUnlock(int playerLevel){
        return playerLevel>=this.unlockLevel;
    }

    // 开始冷却
    public void startCooldown() {
        this.coolDownRemaining = 2;
    }

    // 重置冷却（
    public void resetCooldown() {
        this.coolDownRemaining = 0;
    }

    // 检查是否在冷却中
    public boolean isOnCooldown() {
        return this.coolDownRemaining > 0;
    }

    // 每回合结束后更新冷却
    public void updateCooldown() {
        if (this.coolDownRemaining > 0) {
            this.coolDownRemaining--;
        }
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public String getType() { return type; }
    public int getUnlockLevel() { return unlockLevel; }
    public String getDescription(){ return description; }
}

//玩家类
class Player implements Serializable{
    private String name;
    private double health;
    private double experience;
    private double energy;
    private int level;
    private final double maxHealth=100;
    private final double maxEnergy=100;
    private Map<Skill,Integer> unlockedSkills;
    private List<Skill> allSkills;
    private boolean isDefending;
    private static final long serialVersionUID = 1L;


    public Player(String name){
        this.name=name;
        this.health=maxHealth;
        this.energy=maxEnergy;
        this.level=1;
        this.unlockedSkills=new HashMap<Skill,Integer>();
        this.allSkills=new ArrayList<>();
        this.isDefending = false;
        initializeSkills();
    }

    //初始化所有技能
    private void initializeSkills() {
        allSkills.clear();
        allSkills.add(new Skill("基础攻击", "基本的攻击技能", "攻击", 10, 1));
        allSkills.add(new Skill("精准射击", "远程技能，拉弓瞄准敌人要害", "攻击", 12, 2));
        allSkills.add(new Skill("破甲挥砍", "近战基础技能，双手武器精准横劈", "攻击", 15, 3));
        allSkills.add(new Skill("精准雷暴", "引导雷电能量锁定单个目标，1秒后一道雷电精准劈下", "攻击", 18, 4));
        allSkills.add(new Skill("重武器运用", "使用重型武器的强力一击", "攻击", 22, 5));
        allSkills.add(new Skill("连刺突刺", "近战连招技能，向前快速突进3米，对路径上敌人造成3次连续刺击", "攻击", 26, 6));
        allSkills.add(new Skill("淬毒匕首", "暗杀技能，潜行状态下使用，匕首刺击目标背部", "攻击", 30, 7));
        allSkills.add(new Skill("震荡重击", "近战控制技能，以武器猛击目标脚下地面", "攻击", 35, 8));
        allSkills.add(new Skill("聚焦焰击", "元素攻击技能，将火焰能量凝聚于一点，对单个目标喷射持续2秒的火焰", "攻击", 40, 9));
        allSkills.add(new Skill("穿甲爆破", "远程单体技能，发射一枚凝聚能量的穿甲弹", "攻击", 50, 10));

        // ========== 防御技能 (10个) ==========
        allSkills.add(new Skill("基础防御", "基本的防御技能", "防御", 5, 1));
        allSkills.add(new Skill("定向护盾", "生成一个仅针对当前锁定目标的定向魔法护盾", "防御", 7, 2));
        allSkills.add(new Skill("招架技巧", "将攻击引导至安全方向的技巧", "防御", 9, 3));
        allSkills.add(new Skill("全面防护", "覆盖全身的防御姿势", "防御", 12, 4));
        allSkills.add(new Skill("侧闪", "反应型防御技能", "防御", 15, 5));
        allSkills.add(new Skill("针对性伪装", "隐蔽防御技能，融入环境并屏蔽自身气息", "防御", 18, 6));
        allSkills.add(new Skill("铁壁对峙", "调整姿态专注防御单个目标", "防御", 22, 7));
        allSkills.add(new Skill("伤害分散", "将伤害分散到不同部位", "防御", 26, 8));
        allSkills.add(new Skill("近身防御", "近距离战斗时的防御技巧", "防御", 30, 9));
        allSkills.add(new Skill("紧急规避", "极限防御技能", "防御", 40, 10));

        // ========== 治疗技能 (10个) ==========
        allSkills.add(new Skill("基础治疗", "基本的治疗技能", "治疗", 8, 1));
        allSkills.add(new Skill("止血技巧", "快速止血的医疗技巧", "治疗", 10, 2));
        allSkills.add(new Skill("恢复呼吸", "调整呼吸促进恢复", "治疗", 13, 3));
        allSkills.add(new Skill("紧急救护", "紧急情况下的快速救治", "治疗", 16, 4));
        allSkills.add(new Skill("高效包扎", "使用包扎技术高效恢复", "治疗", 20, 5));
        allSkills.add(new Skill("体能恢复", "恢复体力和能量的方法", "治疗", 24, 6));
        allSkills.add(new Skill("深度治疗", "对身体深处的伤害进行治疗", "治疗", 28, 7));
        allSkills.add(new Skill("净化术", "综合治疗技能，清除身上所有“中毒”“麻痹”“眩晕”等", "治疗", 33, 8));
        allSkills.add(new Skill("全面治愈", "对身体进行全面性恢复", "治疗", 38, 9));
        allSkills.add(new Skill("紧急救援", "极度危险情况下的救命技巧", "治疗", 45, 10));

        updateUnlockedSkills();

    }
    private void updateUnlockedSkills() {
        unlockedSkills.clear();
        for (Skill skill : allSkills) {
            if (skill.canUnlock(level)) {
                unlockedSkills.put(skill, skill.getLevel());
            }
        }
    }
    //使用技能
    public double useSkill(Skill skill){
        skill.startCooldown();
        return skill.getActualValue();
    }
    //承受伤害
    public void takeDamage(double damage){
        this.health=Math.max(0, this.health-damage);
    }
    //恢复生命值
    public void heal(double amount){
        this.health=Math.min(maxHealth,this.health+amount);
    }
    //恢复精力
    public void restoreEnergy(double amount){
        this.energy=Math.min(maxEnergy,this.energy+amount);
    }

    public double getExpToNextLevel() {
        return this.level * 100 - this.experience;
    }
    public void resetHealth() {
        this.health = maxHealth;
    }
    //增加经验
    public void addExperience(double amount){
        this.experience=this.experience+amount;
        if(this.experience>=this.level*100){
            levelUp();
        }
    }
    //升级
    public void levelUp(){
        this.level++;
        this.health=maxHealth;
        this.energy=maxEnergy;
        this.experience=0;
        System.out.println("恭喜！您已升级到"+this.level+"级！");

        updateUnlockedSkills();

        // 显示新解锁的技能
        for(Skill skill : allSkills){
            if(skill.getUnlockLevel() == level){
                System.out.println("解锁新技能："+skill.getName() + " (" + skill.getType() + ")");
            }
        }
    }
    //检查玩家是否存活
    public boolean isAlive(){
        return health>0;
    }
    //获取当前可用技能
    public List<Skill> getAvailableSkills(){
        List<Skill>available=new ArrayList<>();
        for(Skill skill:unlockedSkills.keySet()){
            if(!skill.isOnCooldown()){
                available.add(skill);
            }
        }
        return available;
    }
    // 重置所有技能冷却
    public void resetAllCooldowns() {
        for (Skill skill : unlockedSkills.keySet()) {
            skill.resetCooldown();
        }
    }
    // 设置防御状态（使用防御技能时调用）
    public void setDefending(boolean defending) {
        this.isDefending = defending;
    }

    // 获取防御状态（计算伤害时调用）
    public boolean isDefending() {
        return isDefending;
    }

    public String getName() { return name; }
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }
    public double getEnergy() { return energy; }
    public double getMaxEnergy() { return maxEnergy; }
    public int getLevel() { return level; }
    public double getExperience() { return experience; }
    public Map<Skill, Integer> getUnlockedSkills() { return unlockedSkills; }
}

//NPC类
class NPC {
    private String name;
    private double health;
    private final double maxHealth = 100;
    private int level;
    private String type;
    private List<Skill> skills;
    private double attackProb;
    private double defenseProb;
    private double healProb;
    private boolean isDefending;

    public NPC(String name, int level, String type) {
        this.name = name;
        this.health = maxHealth;
        this.level = level;
        this.type = type;
        this.skills = new ArrayList<>();
        this.isDefending = false;
    }

    //设置行为概率
    void setBehaviorProbabilities(String type) {
        if (type.equals("猛攻型")) {
            this.attackProb = 0.7;
            this.defenseProb = 0.2;
            this.healProb = 0.1;
        }
        if (type.equals("均衡型")) {
            this.attackProb = 0.5;
            this.defenseProb = 0.3;
            this.healProb = 0.2;
        }
        if (type.equals("防守型")) {
            this.attackProb = 0.3;
            this.defenseProb = 0.5;
            this.healProb = 0.2;
        }
    }
    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }
    //选择行动
    public Skill chooseAction(Player player) {
        double currentAttackProb = attackProb;
        double currentDefenseProb = defenseProb;
        double currentHealProb = healProb;
        if (health < 30) {
            currentHealProb += 0.5;
        }
        if (player.getHealth() < 40) {
            currentAttackProb += 0.2;
        }
        //归一化
        double totalProb = currentAttackProb + currentDefenseProb + currentHealProb;
        double normalizedAttackProb = currentAttackProb / totalProb;
        double normalizedDefenseProb = currentDefenseProb / totalProb;
        double normalizedHealProb = currentHealProb / totalProb;

        double random = Math.random();
        String chosenType;

        if (random < normalizedAttackProb) {
            chosenType = "攻击";
        } else if (random < normalizedAttackProb + normalizedDefenseProb) {
            chosenType = "防御";
        } else {
            chosenType = "治疗";
        }

        List<Skill> availableSkills = new ArrayList<>();
        for (Skill skill : skills) {
            if (skill.getLevel() == this.level) {
                availableSkills.add(skill);
            }
        }

        if (!availableSkills.isEmpty()) {
            int index = (int) (Math.random() * availableSkills.size());
            Skill chosenSkill = availableSkills.get(index);
            return chosenSkill;
        }
        return null;
    }

    // 承受伤害
    public void takeDamage(double damage) {
        this.health = Math.max(0, this.health - damage);
    }

    // 恢复生命值
    public void heal(double amount) {
        this.health = Math.min(maxHealth, this.health + amount);
    }

    // 设置防御状态（使用防御技能时调用）
    public void setDefending(boolean defending) {
        this.isDefending = defending;
    }

    // 获取防御状态（计算伤害时调用）
    public boolean isDefending() {
        return isDefending;
    }


    // 检查是否存活
    public boolean isAlive() {
        return health > 0;
    }

    public String getName() { return name; }
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }
    public int getLevel() { return level; }
    public String getType() { return type; }
    public List<Skill> getSkills() { return skills; }

}
//场景类
abstract class Scene{
    public String sceneName;
    public String description;
    public Scene(String sceneName, String description){
        this.sceneName=sceneName;
        this.description=description;
    }
    public abstract void enter(Player player);
    public abstract void exit(Player player);
    public abstract void displayOptions(Player player);

}

//训练场景
class TrainingScene extends Scene {
    private Map<Skill, Integer> trainingCost;
    private int sleepRecoveryRate;
    private int eatRecoveryRate;
    private Map<Skill, Integer> skillPracticeCount;

    public TrainingScene() {
        super("训练场", "在这里可以练习技能提升等级");
        this.sleepRecoveryRate = 100;
        this.eatRecoveryRate = 20;
        this.skillPracticeCount = new HashMap<>();
    }

    @Override
    public void enter(Player player) {
        System.out.println("进入" + sceneName + ": " + description);
    }

    @Override
    public void displayOptions(Player player) {
        System.out.println("\n=== 训练场选项 ===");
        System.out.println("1. 练习技能");
        System.out.println("2. 吃东西");
        System.out.println("3. 睡觉");
    }

    @Override
    public void exit(Player player) {
        System.out.println("离开训练场");
    }

    // 显示可练习的技能列表
    public void listAvailableSkills(Player player) {
        System.out.println("\n可练习的技能:");
        int index = 1;
        for (Skill skill : player.getUnlockedSkills().keySet()) {
            int cost =  skill.getLevel() * 5;
            System.out.println(index+"."+skill.getName() + " (等级: " + player.getUnlockedSkills().get(skill) + ", 消耗精力: " + cost + ")");
            index++;
        }
    }

    //练习技能
    public void trainSkill(Player player, Skill skill) {
        int cost = skill.getLevel() * 5;
        if (player.getEnergy() >= cost) {
            player.restoreEnergy(-cost);

            // 每次练习增加练习次数
            if (!skillPracticeCount.containsKey(skill)) {
                skillPracticeCount.put(skill, 0);
            }

            int currentPracticeCount = skillPracticeCount.get(skill) + 1;
            skillPracticeCount.put(skill, currentPracticeCount);

            System.out.println("\n成功练习 " + skill.getName() + "！练习次数：" + currentPracticeCount + "/3");

            // 检查是否达到3次练习
            if (currentPracticeCount >= 3) {
                skill.levelUp();
                skillPracticeCount.put(skill, 0); // 重置练习次数
                player.getUnlockedSkills().put(skill, skill.getLevel());
                System.out.println("恭喜！" + skill.getName() + " 升级到 " + skill.getLevel() + " 级！");
            }
        } else {
            System.out.println("精力不足！需要 " + cost + " 精力，当前只有 " + player.getEnergy() + "精力");
        }
    }

    //吃东西恢复精力
    public void rest(Player player) {
        player.restoreEnergy(eatRecoveryRate);
        System.out.println("吃东西恢复了 " + eatRecoveryRate + " 精力，现在精力为"+player.getEnergy());
    }
    // 睡觉恢复精力
    public void sleep(Player player) {
        player.restoreEnergy(sleepRecoveryRate);
        System.out.println("睡觉恢复了 " + sleepRecoveryRate + " 精力，现在精力为"+player.getEnergy());
    }
}

//战斗场景
class BattleScene extends Scene {
    NPC opponent;
    private int currentRound;
    private int maxRounds;
    private boolean battleEnded;
    private boolean battleStarted;
    private CombatSystem combatSystem;
    public BattleScene() {
        super("竞技场","在这里与NPC进行战斗");
        this.maxRounds=20;
        this.battleEnded = false;
        this.battleStarted = false;
        this.combatSystem = new CombatSystem();
    }

    @Override
    public void enter(Player player) {
        System.out.println("进入" + sceneName + ": " + description);
        if (!battleStarted) {
            startBattle(player);
        }
    }

    @Override
    public void displayOptions(Player player) {
        if (battleEnded) {
            System.out.println("\n战斗已结束，按任意键返回训练场...");
            return;
        }
        System.out.println("\n=== 战斗选项 ===");
        List<Skill> availableSkills = player.getAvailableSkills();
        for (int i = 0; i < availableSkills.size(); i++) {
            Skill skill = availableSkills.get(i);
            System.out.println((i + 1) + ". 使用 " + skill.getName() + " (类型: " + skill.getType() + ")");
        }
    }
    @Override
    public void exit(Player player) {
        System.out.println("离开竞技场");
    }
    // 开始战斗
    public void startBattle(Player player) {
        this.opponent = NpcMatch.generateNPC(player.getLevel());
        this.currentRound = 0;
        this.battleEnded = false;
        this.battleStarted = true;
        player.resetHealth();
        player.resetAllCooldowns();

        System.out.println("遇到 " + opponent.getName() + " (等级: " + opponent.getLevel() + ", 类型: " + opponent.getType() + ")");
    }
    // 重置战斗状态
    public void resetBattle(Player player) {
        this.battleStarted = false;
        this.battleEnded = false;
        this.currentRound = 0;
    }


    // BattleScene 类中的 playerTurn 方法
    public void playerTurn(Player player, Skill skill) {
        if (battleEnded) return;

        double effect = 0;
        switch (skill.getType()) {
            case "攻击":
                // 确定NPC的防御技能（如果处于防御状态）
                Skill npcDefenseSkill = opponent.isDefending() ? getNpcDefenseSkill() : null;

                // 使用战斗系统计算伤害和防御减免
                double[] damageResult = combatSystem.calculateDamageWithDefense(
                        skill, player.getLevel(), npcDefenseSkill, opponent.getLevel()
                );
                effect = damageResult[0]; // 实际伤害
                double defenseReduction = damageResult[1]; // 防御减免

                opponent.takeDamage(effect);

                // 显示伤害信息
                if (npcDefenseSkill != null && defenseReduction > 0) {
                    System.out.println("你对" + opponent.getName() + "造成了 " +
                            String.format("%.1f", effect + defenseReduction) + " 点伤害，但被抵御了 " +
                            String.format("%.1f", defenseReduction) + " 点，实际造成 " +
                            String.format("%.1f", effect) + " 点伤害！");
                } else {
                    System.out.println("你对" + opponent.getName() + "造成了 " +
                            String.format("%.1f", effect) + " 点伤害！");
                }
                break;
            case "防御":
                player.setDefending(true);
                System.out.println("你进入了防御状态！本回合将抵挡对方部分伤害！");
                break;
            case "治疗":
                effect = skill.getActualValue();
                player.heal(effect);
                System.out.println("你恢复了 " + String.format("%.1f", effect) + " 点生命值！");
                break;
        }

        // 攻击技能进入冷却
        if (skill.getType().equals("攻击")) {
            skill.startCooldown();
        }
    }

    // BattleScene 类中的 npcTurn 方法
    public void npcTurn(Player player) {
        if (battleEnded) return;

        Skill npcSkill = opponent.chooseAction(player);
        if (npcSkill == null) {
            System.out.println(opponent.getName() + " 无法选择行动！");
            return;
        }

        double effect = 0;
        switch (npcSkill.getType()) {
            case "攻击":
                // 确定玩家的防御技能（如果处于防御状态）
                Skill playerDefenseSkill = player.isDefending() ? getPlayerDefenseSkill(player) : null;

                // 使用战斗系统计算伤害和防御减免
                double[] damageResult = combatSystem.calculateDamageWithDefense(
                        npcSkill, opponent.getLevel(), playerDefenseSkill, player.getLevel()
                );
                effect = damageResult[0]; // 实际伤害
                double defenseReduction = damageResult[1]; // 防御减免

                player.takeDamage(effect);

                // 显示伤害信息
                if (playerDefenseSkill != null && defenseReduction > 0) {
                    System.out.println(opponent.getName() + "对你造成了 " +
                            String.format("%.1f", effect + defenseReduction) + " 点伤害，但你抵御了 " +
                            String.format("%.1f", defenseReduction) + " 点，实际受到 " +
                            String.format("%.1f", effect) + " 点伤害！");
                } else {
                    System.out.println(opponent.getName() + "对你造成了 " +
                            String.format("%.1f", effect) + " 点伤害！");
                }
                break;
            case "防御":
                opponent.setDefending(true);
                System.out.println(opponent.getName() + "进入了防御状态！本回合将抵挡你部分伤害！");
                break;

            case "治疗":
                effect = npcSkill.getActualValue();
                opponent.heal(effect);
                System.out.println(opponent.getName() + "恢复了 " + String.format("%.1f", effect) + " 点生命值！");
                break;
        }

        // 攻击技能进入冷却
        if (npcSkill.getType().equals("攻击")) {
            npcSkill.startCooldown();
        }
    }

    public void calculateRoundResult(Player player) {
        currentRound++;
        System.out.println("\n=== 第 " + currentRound + " 回合结束 ===");
        displayBattleStatus(player);
        // 更新技能冷却
        for (Skill skill : player.getUnlockedSkills().keySet()) {
            skill.updateCooldown();
        }
        // 重置双方防御状态（防御只持续一回合）
        player.setDefending(false);
        opponent.setDefending(false);
        checkBattleEnd(player);
    }

    // 显示战斗状态
    public void displayBattleStatus(Player player) {
        System.out.println(player.getName() + ": " + String.format("%.1f", player.getHealth()) + "/" + player.getMaxHealth() + " HP");
        System.out.println(opponent.getName() + ": " + String.format("%.1f", opponent.getHealth()) + "/" + opponent.getMaxHealth() + " HP");
    }

    // 检查战斗是否结束
    public void checkBattleEnd(Player player) {
        if (!player.isAlive()) {
            battleEnded = true;
            System.out.println("你被击败了！");
            player.addExperience(-20*player.getLevel());
        } else if (!opponent.isAlive()) {
            battleEnded = true;
            System.out.println("你击败了 " + opponent.getName() + "！");
            awardExperience(player);
        } else if (currentRound >= maxRounds) {
            battleEnded = true;
            System.out.println("战斗超时！");
            determineTimeoutWinner(player);
        }
    }
    // 发放经验奖励
    public void awardExperience(Player player) {
        int exp = opponent.getLevel() * 25;
        player.addExperience(exp);
        System.out.println("获得 " + exp + " 点经验值！");
    }
    // 判断超时胜负
    public void determineTimeoutWinner(Player player) {
        double playerHealth = (double) player.getHealth();
        double npcHealth = (double) opponent.getHealth();

        if (playerHealth > npcHealth) {
            System.out.println("根据剩余生命值，你获得了胜利！");
            awardExperience(player);
        } else if (playerHealth< npcHealth) {
            System.out.println("根据剩余生命值，你失败了！");
        } else {
            System.out.println("平局！");
        }
    }

    public boolean isBattleEnded() {
        return battleEnded;
    }

    public NPC getOpponent() {
        return opponent;
    }

    public CombatSystem getCombatSystem() {
        return combatSystem;
    }

    public Skill getPlayerDefenseSkill(Player player) {
        for (Skill skill : player.getUnlockedSkills().keySet()) {
            if (skill.getType().equals("防御")) {
                return skill;
            }
        }
        return null;
    }

    public Skill getNpcDefenseSkill() {
        for (Skill skill : opponent.getSkills()) {
            if (skill.getType().equals("防御")) {
                return skill;
            }
        }
        return null;
    }

}

// CombatSystem 类
class CombatSystem {
    private Random random;

    public CombatSystem() {
        this.random = new Random();
    }

    // 计算伤害（考虑攻击和防御技能），返回一个包含[实际伤害, 防御减免]的数组
    public double[] calculateDamageWithDefense(Skill attackSkill, int attackerLevel, Skill defenseSkill, int defenderLevel) {
        // 基础伤害 = 技能效果 * 攻击者等级系数
        double baseDamage = attackSkill.getActualValue() * (1 + 0.1 * attackerLevel);

        double defenseValue = 0;
        double defenseReduction = 0;
        double finalDamage = baseDamage;

        // 如果有防御技能，计算防御减免
        if (defenseSkill != null) {
            defenseValue = defenseSkill.getActualValue() * (1 + 0.1 * defenderLevel);
            defenseReduction = Math.min(baseDamage, defenseValue); // 防御减免不超过基础伤害
            finalDamage = Math.max(0, baseDamage - defenseValue); // 伤害不低于0
        }

        // 添加随机波动 ±15%
        double randomFactor = 0.85 + random.nextDouble() * 0.3;
        finalDamage *= randomFactor;
        defenseReduction *= randomFactor; // 防御减免也受随机因子影响

        return new double[]{finalDamage, defenseReduction};
    }

}

//NPC匹配生成
class NpcMatch{
    private static Random random = new Random();
    public static NPC generateNPC(int playerLevel) {

        int minLevel = Math.max(1, playerLevel - 3);
        int maxLevel = playerLevel + 3;
        int npcLevel = minLevel + random.nextInt(maxLevel - minLevel + 1);


        String type = getRandomNPCType();
        String name = "NPC Lv." + npcLevel + " (" + type + ")";

        NPC npc = new NPC(name, npcLevel, type);
        assignNPCSkills(npc);
        setNPCBehavior(npc, type); // 确保调用setNPCBehavior

        return npc;
    }

    private static void assignNPCSkills(NPC npc) {
        List<Skill> npcSkills = new ArrayList<>();
        int npcLevel = npc.getLevel();

        // 根据NPC等级分配不同技能
        Skill attackSkill = new Skill("NPC攻击", "NPC攻击技能", "攻击", (int)(5 + npcLevel * 0.8), 1);
        attackSkill.setLevel(npcLevel);  // 设置技能等级

        Skill defenseSkill = new Skill("NPC防御", "NPC防御技能", "防御", (int)(4+ npcLevel * 0.8), 1);
        defenseSkill.setLevel(npcLevel);  // 设置技能等级

        npcSkills.add(attackSkill);
        npcSkills.add(defenseSkill);

        if (npcLevel >= 3) {
            Skill healSkill = new Skill("NPC治疗", "NPC治疗技能", "治疗", (int)(6+ npcLevel * 0.8), 1);
            healSkill.setLevel(npcLevel);  // 设置技能等级
            npcSkills.add(healSkill);
        }

        // 设置NPC技能列表
        npc.setSkills(npcSkills);
    }
    // 设置NPC行为概率
    private static void setNPCBehavior(NPC npc, String type) {
        npc.setBehaviorProbabilities(type);
    }
    private static String getRandomNPCType() {
        String[] types = {"猛攻型","均衡型","防御型"};
        return types[random.nextInt(types.length)];
    }
}

//游戏管理器
class GameManager {
    private Player player;
    private Scene currentScene;
    private Scanner scanner;

    public GameManager() {
        this.scanner = new Scanner(System.in);
    }
    //开始游戏
    public void startGame() {
        System.out.println("欢迎来到MUD游戏！");
        System.out.print("请输入玩家名称: ");
        String playerName = scanner.nextLine();

        // 尝试加载已有存档
        Player loadedPlayer = loadPlayer(playerName);
        if (loadedPlayer != null) {
            System.out.println("检测到存档，已加载！");
            this.player = loadedPlayer;
        } else {
            System.out.println("未检测到存档，创建新角色！");
            this.player = new Player(playerName);
        }

        mainLoop();
    }

    private void handleBattleChoice(int choice, BattleScene battleScene) {
        if (battleScene.isBattleEnded()) {
            System.out.println("战斗结束，返回主菜单...");
            return;
        }

        List<Skill> availableSkills = player.getAvailableSkills();
        if (choice > 0 && choice <= availableSkills.size()) {
            Skill selectedSkill = availableSkills.get(choice - 1);

            processPlayerAction(battleScene, player, selectedSkill);

            Skill npcSkill = processNPCAction(battleScene, player);

            executeDamageCalculations(battleScene, player, selectedSkill, npcSkill);

            battleScene.calculateRoundResult(player);

            if (battleScene.isBattleEnded()) {
                System.out.println("\n战斗结束！");
            }
        } else {
            System.out.println("无效选择！");
        }
    }

    // 处理玩家行动（设置状态）
    private void processPlayerAction(BattleScene battleScene, Player player, Skill skill) {
        switch (skill.getType()) {
            case "防御":
                player.setDefending(true);
                double defenseValue = skill.getActualValue();
                System.out.println("你使用了" + skill.getName() +
                        "，进入了防御状态！");
                break;
            case "治疗":
                // 治疗立即生效
                double healValue = skill.getActualValue();
                player.heal(healValue);
                System.out.println("你恢复了 " + String.format("%.1f", healValue) + " 点生命值！");
                break;
            case "攻击":
                // 攻击行动只记录，稍后计算
                break;
        }
    }

    // 处理NPC行动（设置状态）
    private Skill processNPCAction(BattleScene battleScene, Player player) {
        NPC opponent = battleScene.getOpponent();
        Skill npcSkill = opponent.chooseAction(player);

        if (npcSkill == null) {
            System.out.println(opponent.getName() + " 无法选择行动！");
            return null;
        }

        switch (npcSkill.getType()) {
            case "防御":
                opponent.setDefending(true);
                double defenseValue = npcSkill.getActualValue();
                System.out.println(opponent.getName() + "使用了" + npcSkill.getName() +
                        "，进入了防御状态！");
                break;
            case "治疗":
                // 治疗立即生效
                double healValue = npcSkill.getActualValue();
                opponent.heal(healValue);
                System.out.println(opponent.getName() + "恢复了 " + String.format("%.1f", healValue) + " 点生命值！");
                break;
            case "攻击":
                break;
        }

        return npcSkill;
    }

    // 执行伤害计算
    private void executeDamageCalculations(BattleScene battleScene, Player player, Skill playerSkill, Skill npcSkill) {
        NPC opponent = battleScene.getOpponent();

        // 执行玩家攻击（如果有）
        if (playerSkill != null && playerSkill.getType().equals("攻击")) {
            // 确定NPC的防御技能（如果处于防御状态）
            Skill npcDefenseSkill = opponent.isDefending() ? battleScene.getNpcDefenseSkill() : null;

            // 使用战斗系统计算伤害和防御减免
            double[] damageResult = battleScene.getCombatSystem().calculateDamageWithDefense(
                    playerSkill, player.getLevel(), npcDefenseSkill, opponent.getLevel()
            );
            double effect = damageResult[0]; // 实际伤害
            double defenseReduction = damageResult[1]; // 防御减免

            opponent.takeDamage(effect);

            // 显示伤害信息
            if (npcDefenseSkill != null && defenseReduction > 0) {
                System.out.println("你对" + opponent.getName() + "造成了 " +
                        String.format("%.1f", effect + defenseReduction) + " 点伤害，但被抵御了 " +
                        String.format("%.1f", defenseReduction) + " 点，实际造成 " +
                        String.format("%.1f", effect) + " 点伤害！");
            } else {
                System.out.println("你对" + opponent.getName() + "造成了 " +
                        String.format("%.1f", effect) + " 点伤害！");
            }

            // 攻击技能进入冷却
            playerSkill.startCooldown();
        }

        // 执行NPC攻击（如果有）
        if (npcSkill != null && npcSkill.getType().equals("攻击")) {
            // 确定玩家的防御技能（如果处于防御状态）
            Skill playerDefenseSkill = player.isDefending() ? battleScene.getPlayerDefenseSkill(player) : null;

            // 使用战斗系统计算伤害和防御减免
            double[] damageResult = battleScene.getCombatSystem().calculateDamageWithDefense(
                    npcSkill, opponent.getLevel(), playerDefenseSkill, player.getLevel()
            );
            double effect = damageResult[0]; // 实际伤害
            double defenseReduction = damageResult[1]; // 防御减免

            player.takeDamage(effect);

            // 显示伤害信息
            if (playerDefenseSkill != null && defenseReduction > 0) {
                System.out.println(opponent.getName() + "对你造成了 " +
                        String.format("%.1f", effect + defenseReduction) + " 点伤害，但你抵御了 " +
                        String.format("%.1f", defenseReduction) + " 点，实际受到 " +
                        String.format("%.1f", effect) + " 点伤害！");
            } else {
                System.out.println(opponent.getName() + "对你造成了 " +
                        String.format("%.1f", effect) + " 点伤害！");
            }

            // 攻击技能进入冷却
            npcSkill.startCooldown();
        }
    }

    //主菜单
    public void mainLoop() {
        boolean running = true;

        while (running && player.isAlive()) {
            // 显示主菜单
            displayMainMenu();
            int choice = getPlayerChoice();

            // 处理主菜单选择
            switch (choice) {
                case 1: // 进入训练场
                    enterTrainingScene();
                    break;
                case 2: // 进入竞技场
                    enterBattleScene();
                    break;
                case 3: // 查看个人信息
                    displayPlayerInfo(player);
                    break;
                case 4: // 退出游戏
                    running = false;
                    savePlayer();
                    System.out.println("感谢游玩，再见！");
                    break;
                default:
                    System.out.println("无效选择，请重新输入！");
            }
        }

        if (!player.isAlive()) {
            System.out.println("游戏结束！");
        }

        scanner.close();
    }

    private void displayMainMenu() {
        System.out.println("\n=== 主菜单 ===");
        System.out.println("1. 进入训练场");
        System.out.println("2. 进入竞技场");
        System.out.println("3. 查看个人信息");
        System.out.println("4. 退出游戏");
    }
    // 进入训练场场景的处理
    private void enterTrainingScene() {
        TrainingScene trainingScene = new TrainingScene();
        trainingScene.enter(player);

        boolean inTraining = true;
        while (inTraining && player.isAlive()) {
            trainingScene.displayOptions(player);
            int choice = getPlayerChoice();

            // 处理训练场选项
            switch (choice) {
                case 1: // 练习技能
                    handleSkillTraining(trainingScene);
                    break;
                case 2: // 吃东西
                    trainingScene.rest(player);
                    break;
                case 3: // 睡觉
                    trainingScene.sleep(player);
                    break;
                default:
                    System.out.println("无效选择！");
            }

            // 询问是否继续在训练场
            System.out.print("是否继续在训练场操作？(1=是, 0=返回主菜单): ");
            int continueChoice = getPlayerChoice();
            if (continueChoice == 0) {
                inTraining = false;
                trainingScene.exit(player);
            }
        }
    }
    //进入竞技场场景的处理
    private void enterBattleScene() {
        BattleScene battleScene = new BattleScene();
        battleScene.enter(player);

        // 战斗结束后自动返回主菜单
        boolean inBattle = true;
        while (inBattle && player.isAlive() && !battleScene.isBattleEnded()) {
            battleScene.displayOptions(player);
            int choice = getPlayerChoice();
            handleBattleChoice(choice, battleScene);

            if (battleScene.isBattleEnded()) {
                inBattle = false;
                battleScene.exit(player);
                System.out.println("返回主菜单...");
            }
        }
    }

    // 获取玩家选择
    private int getPlayerChoice() {
        System.out.print("请选择操作: ");
        while (!scanner.hasNextInt()) {
            System.out.print("请输入有效数字: ");
            scanner.next();
        }
        return scanner.nextInt();
    }

    private void handleSkillTraining(TrainingScene trainingScene) {
        // 获取玩家已解锁的技能列表
        List<Skill> availableSkills = new ArrayList<>(player.getUnlockedSkills().keySet());

        if (availableSkills.isEmpty()) {
            System.out.println("没有可练习的技能！");
            return;
        }

        // 显示详细的技能信息
        System.out.println("\n=== 可练习的技能列表 ===");
        for (int i = 0; i < availableSkills.size(); i++) {
            Skill skill = availableSkills.get(i);
            int cost = skill.getLevel() * 5;
            int nextLevel = skill.getLevel() + 1;

            System.out.println((i + 1) + ". " + skill.getName());
            System.out.println("   描述: " + skill.getDescription());
            System.out.println("   类型: " + skill.getType());
            System.out.println("   当前等级: " + skill.getLevel());
            System.out.println("   下一等级: " + nextLevel);
            System.out.println("   消耗精力: " + cost);
            System.out.println(); // 空行
        }

        System.out.print("选择要练习的技能编号 (1-" + availableSkills.size() + ")，输入0返回: ");
        int skillChoice = getPlayerChoice();

        if (skillChoice == 0) {
            return; // 返回上级菜单
        }

        // 验证输入是否有效
        if (skillChoice > 0 && skillChoice <= availableSkills.size()) {
            Skill selectedSkill = availableSkills.get(skillChoice - 1);

            // 检查精力是否足够
            int cost = selectedSkill.getLevel() * 5;
            if (player.getEnergy() >= cost) {
                System.out.println("\n确认练习 " + selectedSkill.getName() + "?");
                System.out.println("将消耗 " + cost + " 精力，当前精力: " + player.getEnergy());
                System.out.print("确认练习? (1=是, 0=否): ");

                int confirm = getPlayerChoice();
                if (confirm == 1) {
                    trainingScene.trainSkill(player, selectedSkill);
                } else {
                    System.out.println("取消练习。");
                }
            } else {
                System.out.println("精力不足！需要 " + cost + " 精力，当前只有 " + player.getEnergy() + " 精力");
                System.out.println("请先通过吃东西或睡觉恢复精力。");
            }
        } else {
            System.out.println("无效的技能选择！");
        }
    }

    private void displayPlayerInfo(Player player) {
        System.out.println("\n=== 个人信息 ===");
        System.out.println("姓名: " + player.getName());
        System.out.println("等级: " + player.getLevel());
        System.out.println("当前经验值: " + String.format("%.1f", player.getExperience()));
        System.out.println("距离下一等级还需: " + String.format("%.1f", player.getExpToNextLevel()) + " 经验值");
        System.out.println("生命值: " + String.format("%.1f", player.getHealth()) + "/" + player.getMaxHealth());
        System.out.println("精力值: " + String.format("%.1f", player.getEnergy()) + "/" + player.getMaxEnergy());

        System.out.println("\n=== 已解锁技能 ===");
        List<Skill> unlockedSkills = new ArrayList<>(player.getUnlockedSkills().keySet());
        if (unlockedSkills.isEmpty()) {
            System.out.println("暂无解锁技能");
        } else {
            for (int i = 0; i < unlockedSkills.size(); i++) {
                Skill skill = unlockedSkills.get(i);
                System.out.println((i + 1) + ". " + skill.getName());
                System.out.println("   等级: " + skill.getLevel());
                System.out.println("   类型: " + skill.getType());
                System.out.println("   描述: " + skill.getDescription());
                System.out.println("   基础效果: " + String.format("%.1f", skill.calculateEffect()));
            }
        }
        System.out.println("\n按任意键返回...");
        scanner.next(); // 等待用户输入
    }

    private void handleBattleChoice(int choice) {
        BattleScene battleScene = (BattleScene) currentScene;

        // 如果战斗已结束，返回训练场
        if (battleScene.isBattleEnded()) {
            System.out.println("战斗结束，返回训练场...");
            changeToTrainingScene();
            battleScene.resetBattle(player); // 重置战斗状态
            return;
        }

        List<Skill> availableSkills = player.getAvailableSkills();
        if (choice > 0 && choice <= availableSkills.size()) {
            Skill selectedSkill = availableSkills.get(choice - 1);

            // 执行玩家回合
            battleScene.playerTurn(player, selectedSkill);

            // 检查战斗是否在玩家回合结束
            if (!battleScene.isBattleEnded()) {
                // NPC回合
                battleScene.npcTurn(player);
                // 计算回合结果并检查战斗是否结束
                battleScene.calculateRoundResult(player);
            }
            // 如果战斗结束，提示玩家
            if (battleScene.isBattleEnded()) {
                System.out.println("\n战斗结束！");
            }
        } else {
            System.out.println("无效选择！");
        }
    }
    // 场景切换方法
    private void changeToBattleScene() {
        currentScene = new BattleScene();
    }


    private void changeToTrainingScene() {
        currentScene = new TrainingScene();
    }

    private void savePlayer() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(player.getName() + ".dat"))) {
            oos.writeObject(player);
            System.out.println("游戏数据已保存");
        } catch (IOException e) {
            System.out.println("保存失败: " + e.getMessage());
        }
    }

    private Player loadPlayer(String username) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(username + ".dat"))) {
            return (Player) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

}
