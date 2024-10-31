package me.quickscythe.vanillaflux.utils.data;

import java.util.HashMap;
import java.util.Map;

public class BinaryUtils {

    Map<Character, Integer> key = new HashMap<>(Map.ofEntries(
            Map.entry('a', 1),
            Map.entry('b', 2),
            Map.entry('c', 3),
            Map.entry('d', 4),
            Map.entry('e', 5),
            Map.entry('f', 6),
            Map.entry('g', 7),
            Map.entry('h', 8),
            Map.entry('i', 9),
            Map.entry('j', 10),
            Map.entry('k', 11),
            Map.entry('l', 12),
            Map.entry('m', 13),
            Map.entry('n', 14),
            Map.entry('o', 15),
            Map.entry('p', 16),
            Map.entry('q', 17),
            Map.entry('r', 18),
            Map.entry('s', 19),
            Map.entry('t', 20),
            Map.entry('u', 21),
            Map.entry('v', 22),
            Map.entry('w', 23),
            Map.entry('x', 24),
            Map.entry('y', 25),
            Map.entry('z', 26),
            Map.entry('A', 30),
            Map.entry('B', 31),
            Map.entry('C', 32),
            Map.entry('D', 33),
            Map.entry('E', 34),
            Map.entry('F', 35),
            Map.entry('G', 36),
            Map.entry('H', 37),
            Map.entry('I', 38),
            Map.entry('J', 39),
            Map.entry('K', 40),
            Map.entry('L', 41),
            Map.entry('M', 42),
            Map.entry('N', 43),
            Map.entry('O', 44),
            Map.entry('P', 45),
            Map.entry('Q', 46),
            Map.entry('R', 47),
            Map.entry('S', 48),
            Map.entry('T', 49),
            Map.entry('U', 50),
            Map.entry('V', 51),
            Map.entry('W', 52),
            Map.entry('X', 53),
            Map.entry('Y', 54),
            Map.entry('Z', 55),
            Map.entry('0', 60),
            Map.entry('1', 61),
            Map.entry('2', 62),
            Map.entry('3', 63),
            Map.entry('4', 64),
            Map.entry('5', 65),
            Map.entry('6', 66),
            Map.entry('7', 67),
            Map.entry('8', 68),
            Map.entry('9', 69),
            Map.entry('!', 70),
            Map.entry('@', 71),
            Map.entry('#', 72),
            Map.entry('$', 73),
            Map.entry('%', 74),
            Map.entry('^', 75),
            Map.entry('&', 76),
            Map.entry('*', 77),
            Map.entry('(', 78),
            Map.entry(')', 79),
            Map.entry('-', 80),
            Map.entry('_', 81),
            Map.entry('=', 82),
            Map.entry('+', 83),
            Map.entry('[', 84),
            Map.entry(']', 85),
            Map.entry('{', 86),
            Map.entry('}', 87),
            Map.entry('|', 88),
            Map.entry(';', 89),
            Map.entry(':', 90),
            Map.entry('\'', 91),
            Map.entry('"', 92),
            Map.entry(',', 93),
            Map.entry('.', 94),
            Map.entry('<', 95),
            Map.entry('>', 96),
            Map.entry('/', 97),
            Map.entry('?', 98),
            Map.entry('\\', 99),
            Map.entry(' ', 100)
    ));

    public String getBinary(String input) {
        StringBuilder binary = new StringBuilder();
        for (char c : input.toCharArray()) {
            binary.append(Integer.toBinaryString(key.get(c))).append(" ");
        }
        return binary.toString();
    }

    public String fromBinary(String input) {
        StringBuilder text = new StringBuilder();
        for (String s : input.split(" ")) {
            for (Map.Entry<Character, Integer> entry : key.entrySet()) {
                if (Integer.toBinaryString(entry.getValue()).equals(s)) {
                    text.append(entry.getKey());
                    break;
                }
            }
        }
        return text.toString();
    }

}
