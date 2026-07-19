import pathlib, sys

path = pathlib.Path("app/src/main/java/com/unbounded/input/RuleLoader.java")
text = path.read_text(encoding="utf-8")

# 补丁1：KeyDef 加 x/y/w/h 字段
old1 = """    public static class KeyDef {
        public String explicitLabel;
        public float span = 1f;
        public Command tap;"""
new1 = """    public static class KeyDef {
        public String explicitLabel;
        public float span = 1f;
        public Float x, y, w, h; // 百分比坐标（0-100），四者都非null才生效
        public Command tap;"""
assert text.count(old1) == 1, "补丁1锚点未找到或不唯一"
text = text.replace(old1, new1, 1)

# 补丁2：parseKeyDef 里解析 x/y/w/h
old2 = """        key.span = (float) obj.optDouble("span", 1f);
        key.tap = parseCommand(obj.optJSONObject("tap"));"""
new2 = """        key.span = (float) obj.optDouble("span", 1f);
        if (obj.has("x") && obj.has("y") && obj.has("w") && obj.has("h")) {
            key.x = (float) obj.optDouble("x");
            key.y = (float) obj.optDouble("y");
            key.w = (float) obj.optDouble("w");
            key.h = (float) obj.optDouble("h");
        }
        key.tap = parseCommand(obj.optJSONObject("tap"));"""
assert text.count(old2) == 1, "补丁2锚点未找到或不唯一"
text = text.replace(old2, new2, 1)

# 补丁3：toKeyModel() 按是否有完整 x/y/w/h 分支构造 KeyModel
old3 = """        public KeyModel toKeyModel() {
            String id = explicitLabel != null ? explicitLabel : label();
            float s = span > 0 ? span : 1f;
            KeyModel m = new KeyModel(id, id, s, 2, 2, 2, 2);
            m.tap = tap;"""
new3 = """        public KeyModel toKeyModel() {
            String id = explicitLabel != null ? explicitLabel : label();
            float s = span > 0 ? span : 1f;
            boolean hasPercent = x != null && y != null && w != null && h != null;
            KeyModel m = hasPercent
                ? new KeyModel(id, id, s, 2, 2, 2, 2, true, x, y, w, h)
                : new KeyModel(id, id, s, 2, 2, 2, 2);
            m.tap = tap;"""
assert text.count(old3) == 1, "补丁3锚点未找到或不唯一"
text = text.replace(old3, new3, 1)

path.write_text(text, encoding="utf-8")
print("RuleLoader.java 打补丁完成，3处全部成功")
