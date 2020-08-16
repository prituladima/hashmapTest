import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import androidx.collection.ArrayMap;
import androidx.collection.ArraySet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public class ManualTests {
    public static void main(String[] args) throws IOException {

//        System.out.println(get(new char[]{'A', 'H', 'A', '\0', 'H'}));
//        if (true) return;

        long globalStart = System.currentTimeMillis();
        final int[] PUTS = {1, 100};
        final int[] TEST = {1, 15};
        final int TEST_STEPS = 100;
        final int WARM_UP = 10;
        String uuidIgnore = uuid();

        char[][][] results = new char[PUTS[1]][TEST[1]][WARM_UP];
        //Amount of puts per one Map instance
        for (int k = 0; k < WARM_UP; k++)
            for (int puts = PUTS[0]; puts < PUTS[1]; puts++) {
                //Amount of hundred tests
                for (int tests = TEST[0]; tests < TEST[1]; tests++) {
                    long hashMapRes;

                    long start = System.nanoTime();
                    for (int i = 0; i < TEST_STEPS * tests; i++) {
                        Map<String, String> stringStringMap = new HashMap<>(0);
                        for (int j = 0; j < puts; j++) {
                            stringStringMap.put(uuid(), uuid());
                        }
                    }
                    long end = System.nanoTime();
                    hashMapRes = end - start;
//                System.out.printf("%d ", hashMapRes = end - start);

                    long arrayMapRes;
                    start = System.nanoTime();
                    for (int i = 0; i < TEST_STEPS * tests; i++) {
                        Map<String, String> stringStringMap = new ArrayMap<>();
                        for (int j = 0; j < puts; j++) {
                            stringStringMap.put(uuid(), uuid());
                        }
                    }
                    end = System.nanoTime();
//                System.out.printf("%d ", arrayMapRes = end - start);
                    arrayMapRes = end - start;
                    if (Math.abs(hashMapRes - arrayMapRes) < 100) {
                        results[puts][tests][k] = '\0';
                    } else {
                        results[puts][tests][k] = hashMapRes > arrayMapRes ? 'A' : 'H';
                    }

                }
//            System.out.println();
            }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Maps bench");


        int firstColumnCount = 0;
        Row firstRow = sheet.createRow(0);
        for (int tests = TEST[0]; tests < TEST[1]; tests++) {
            firstRow.createCell(++firstColumnCount)
                    .setCellValue(String.valueOf(TEST_STEPS * tests));
        }

        final CellStyle backgroundStyleRed = workbook.createCellStyle();
        backgroundStyleRed.setFillForegroundColor(IndexedColors.RED.getIndex());
        final CellStyle backgroundStyleGreen = workbook.createCellStyle();
        backgroundStyleGreen.setFillForegroundColor(IndexedColors.RED.getIndex());


        int rowCount = 0;
        for (int puts = PUTS[0]; puts < PUTS[1]; puts++) {
            Row row = sheet.createRow(++rowCount);
            row.createCell(0).setCellValue(puts);
            int columnCount = 0;
            for (int tests = TEST[0]; tests < TEST[1]; tests++) {
                char res = get(Arrays.copyOfRange(results[puts][tests], WARM_UP / 2, WARM_UP));
                String s = String.valueOf(res == '\0' ? "" : res);
                Cell cell = row.createCell(++columnCount);
                cell.setCellValue(s);

                if (!s.isEmpty()) {
                    if (s.equals("H")) {
                        cell.setCellStyle(backgroundStyleRed);
                    } else {
                        cell.setCellStyle(backgroundStyleGreen);
                    }
                }
            }
        }


        try (FileOutputStream outputStream = new FileOutputStream("Maps" + new Date().getTime() + ".xlsx")) {
            workbook.write(outputStream);
        }

        System.out.println("Time needed in min: " + (System.currentTimeMillis() - globalStart) / (60.0 * 1000));
    }

    protected static char get(char[] results) {
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
        if (maxChar != '\0' && multiSet.getOrDefault('H', 0) >= multiSet.getOrDefault('A', 0)) {
            return 'H';
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