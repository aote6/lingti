import pathlib
path = pathlib.Path("app/src/main/java/com/unbounded/input/RuleLoader.java")
text = path.read_text(encoding="utf-8")

# 1. parseCommand 加两个case
old1 = """                return KeyChordCommand.of(codes);
            }
            default: return null;"""
new1 = """                return KeyChordCommand.of(codes);
            }
            case "clipboard_open_panel": return Command.clipboardOpenPanel();
            case "clipboard_paste_recent": return Command.clipboardPasteRecent();
            default: return null;"""
assert text.count(old1) == 1, "锚点1未找到或不唯一"
text = text.replace(old1, new1, 1)

# 2. serializeCommand 加两个case
old2 = """            case KEY_CHORD:
                if (cmd instanceof KeyChordCommand) {
                    KeyChordCommand kcc = (KeyChordCommand) cmd;
                    obj.put("type", "key_chord");
                    JSONArray codes = new JSONArray();
                    for (int c : kcc.keyCodes) codes.put(c);
                    obj.put("keyCodes", codes);
                    if (kcc.metaState != 0) obj.put("metaState", kcc.metaState);
                    return obj;
                }
                return null;
            default:
                return null;"""
new2 = """            case KEY_CHORD:
                if (cmd instanceof KeyChordCommand) {
                    KeyChordCommand kcc = (KeyChordCommand) cmd;
                    obj.put("type", "key_chord");
                    JSONArray codes = new JSONArray();
                    for (int c : kcc.keyCodes) codes.put(c);
                    obj.put("keyCodes", codes);
                    if (kcc.metaState != 0) obj.put("metaState", kcc.metaState);
                    return obj;
                }
                return null;
            case CLIPBOARD_OPEN_PANEL:
                obj.put("type", "clipboard_open_panel");
                return obj;
            case CLIPBOARD_PASTE_RECENT:
                obj.put("type", "clipboard_paste_recent");
                return obj;
            default:
                return null;"""
assert text.count(old2) == 1, "锚点2未找到或不唯一"
text = text.replace(old2, new2, 1)

# 3. 更新过时注释
old3 = """    // 和 parseCommand() 对称：把 Command 对象还原成 JSON。
    // 注意：CLIPBOARD_* 类型不支持往返序列化（parseCommand 本来就不认识这个type），
    // 剪贴板键目前是靠 label 硬匹配触发的，不受影响。"""
new3 = """    // 和 parseCommand() 对称：把 Command 对象还原成 JSON。
    // CLIPBOARD_OPEN_PANEL / CLIPBOARD_PASTE_RECENT 已支持往返序列化（2026-07-27修复）。"""
assert text.count(old3) == 1, "锚点3未找到或不唯一"
text = text.replace(old3, new3, 1)

path.write_text(text, encoding="utf-8")
print("RuleLoader.java 打补丁完成")
