import androidx.collection.ArrayMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.apache.poi.ss.usermodel.ComparisonOperator.EQUAL;


public class ManualTests {
    public static void main(String[] args) throws IOException {

        final long globalStart = System.currentTimeMillis();

        final int[] PUTS = {1, 300};
        final int[] TEST = {1, 10};
        final int TEST_STEPS = 100;
        final int WARN_UP_ITERATIONS = 5;
        final int NUMBER_OF_ITERATIONS = 10;
        final String range = String.format("A1:%s%s", getColumn(TEST[1] + 1), PUTS[1] + 1);

        String uuidIgnore = uuid();

        final char[][][] results = new char[PUTS[1]][TEST[1]][NUMBER_OF_ITERATIONS];


        final String[] keys = new String[TEST_STEPS * TEST[1]];
        final String[] values = new String[TEST_STEPS * TEST[1]];
        for (int i = 0; i < TEST_STEPS * TEST[1]; i++) {
            keys[i] = uuid();
            values[i] = uuid();
        }

        //First 5 iterations are for warm-up and are not counted;
        //Anything else are counted and aggregated as a one result;
        for (int iteration = 0; iteration < NUMBER_OF_ITERATIONS; iteration++) {

            for (int puts = PUTS[0]; puts < PUTS[1]; puts++) {

                //We gonna create
                for (int tests = TEST[0]; tests < TEST[1]; tests++) {

                    //Start working with HashMap
                    final long hashMapRes;

                    long start = System.nanoTime();
                    for (int i = 0; i < TEST_STEPS * tests; i++) {
                        Map<String, String> stringStringMap = new HashMap<>(0);
                        for (int j = 0; j < puts; j++) {
                            stringStringMap.put(keys[i], values[i]);
                        }
                    }
                    long end = System.nanoTime();
                    hashMapRes = end - start;
                    //End working with HashMap


                    //Start working with ArrayMap
                    final long arrayMapRes;
                    start = System.nanoTime();
                    for (int i = 0; i < TEST_STEPS * tests; i++) {
                        Map<String, String> stringStringMap = new ArrayMap<>();
                        for (int j = 0; j < puts; j++) {
                            stringStringMap.put(keys[i], values[i]);
                        }
                    }
                    end = System.nanoTime();

                    arrayMapRes = end - start;
                    //End working with ArrayMap


//                    if (Math.abs(hashMapRes - arrayMapRes) < 50) {
//                        results[puts][tests][k] = '\0';
//                    } else {
                    results[puts][tests][iteration] = hashMapRes > arrayMapRes ? 'A' : 'H';
//                    }

                }

            }
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        for (int iteration = 0; iteration < NUMBER_OF_ITERATIONS + 1; iteration++) {


            String name = String.format("%s #%d.", iteration < WARN_UP_ITERATIONS ? "Warm up" : "Iteration", iteration);
            XSSFSheet sheet = workbook.createSheet(iteration == NUMBER_OF_ITERATIONS ? "Aggregated" : name);

            int firstColumnCount = 0;
            Row firstRow = sheet.createRow(0);
            for (int tests = TEST[0]; tests < TEST[1]; tests++) {
                firstRow.createCell(++firstColumnCount)
                        .setCellValue(String.valueOf(TEST_STEPS * tests));
            }

            int rowCount = 0;
            for (int puts = PUTS[0]; puts < PUTS[1]; puts++) {
                Row row = sheet.createRow(++rowCount);
                row.createCell(0).setCellValue(puts);
                int columnCount = 0;
                for (int tests = TEST[0]; tests < TEST[1]; tests++) {
                    char res;
                    if (iteration == NUMBER_OF_ITERATIONS) {
                        res = getMostRepeatingCharacter(Arrays.copyOfRange(results[puts][tests], WARN_UP_ITERATIONS, NUMBER_OF_ITERATIONS));
                    } else {
                        res = results[puts][tests][iteration];
                    }
                    String s = String.valueOf(res == '\0' ? "" : res);
                    Cell cell = row.createCell(++columnCount);
                    cell.setCellValue(s);

                }
            }
            SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

            ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(EQUAL, "\"H\"");
            PatternFormatting fill1 = rule1.createPatternFormatting();
            fill1.setFillBackgroundColor(IndexedColors.RED.index);
            fill1.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

            ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(EQUAL, "\"A\"");
            PatternFormatting fill2 = rule2.createPatternFormatting();
            fill2.setFillBackgroundColor(IndexedColors.GREEN.index);
            fill2.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

            CellRangeAddress[] regions = {
                    CellRangeAddress.valueOf(range)
            };
            sheetCF.addConditionalFormatting(regions, rule1, rule2);

        }

        try (FileOutputStream outputStream = new FileOutputStream("HashMap and ArrayMap heat map. From " + new Date().getTime() + ".xlsx")) {
            workbook.write(outputStream);
        }

        System.out.printf("Time needed in min: %f%n", (System.currentTimeMillis() - globalStart) / (60.0 * 1000));
    }

    protected static String getColumn(int columnNumber) {

        StringBuilder columnName = new StringBuilder();
        while (columnNumber > 0) {
            int rem = columnNumber % 26;
            if (rem == 0) {
                columnName.append("Z");
                columnNumber = (columnNumber / 26) - 1;
            } else {
                columnName.append((char) ((rem - 1) + 'A'));
                columnNumber = columnNumber / 26;
            }
        }
        return columnName.reverse().toString();
    }

    protected static char getMostRepeatingCharacter(char[] results) {
        Map<Character, Integer> multiSet = new HashMap<>();
        for (char result : results) {
            multiSet.merge(result, 1, Integer::sum);
        }
        int max = 0;
        char maxChar = '\0';
        for (char ch : multiSet.keySet()) {
            int val = multiSet.get(ch);
            if (val > max) {
                max = val;
                maxChar = ch;
            }
        }
        if (multiSet.getOrDefault('H', 0).equals(multiSet.getOrDefault('A', 0))) {
            return '\0';
        }
        return maxChar;
    }

    private static String uuid() {
        return UUID.randomUUID().toString();
    }


}
//https://medium.com/@mohom.r/optimising-android-app-performance-with-arraymap-9296f4a1f9eb
//Recommended Data Structures:
//ArrayMap<K,V> for HashMap<K,V>
//ArraySet<K,V> for HashSet<K,V>
//SparseArray<V> for HashMap<Integer,V>
//SparseBooleanArray for HashMap<Integer,Boolean>
//SparseIntArray for HashMap<Integer,Integer>
//SparseLongArray for HashMap<Integer,Long>
//LongSparseArray<V> for HashMap<Long,V>