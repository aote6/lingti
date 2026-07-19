import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/RuleLoader.java")
text = path.read_text(encoding="utf-8")

old = """    private static final Map<String, LayoutConfig> cache = new HashMap<>();"""
new = """    private static final Map<String, LayoutConfig> cache = new HashMap<>();

    // 还原出厂布局用：强制清掉内存缓存，否则删了外部JSON文件后
    // 仍会返回上一次已解析过的缓存对象，看起来像"没有真正还原"。
    public static void clearCache(String fileName) {
        cache.remove(fileName);
    }"""
assert text.count(old) == 1, "锚点未找到或不唯一"
text = text.replace(old, new, 1)

path.write_text(text, encoding="utf-8")
print("RuleLoader.java 打补丁完成")
