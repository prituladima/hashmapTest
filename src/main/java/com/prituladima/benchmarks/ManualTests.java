package com.prituladima.benchmarks;

import androidx.collection.ArrayMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.poi.ss.usermodel.ComparisonOperator.EQUAL;


public class ManualTests {

    public static void main(String[] args) throws IOException, InterruptedException {

        final int nThreads = 2 * Runtime.getRuntime().availableProcessors() - 1;

        final long globalStart = System.currentTimeMillis();

        final int[] MAP_SIZE = {1, 5};
        final int[] TEST = {10000, 10001};
        final int TEST_STEPS = 100;

        final int WARN_UP_ITERATIONS = 0;
        final int NUMBER_OF_ITERATIONS = 32;

        final char[][][] resultsMemo = new char[MAP_SIZE[1]][TEST[1]][NUMBER_OF_ITERATIONS];

        //Pre-generate data
        final String[] keys = new String[MAP_SIZE[1]];
        final String[] values = new String[MAP_SIZE[1]];
        for (int i = 0; i < MAP_SIZE[1]; i++) {
            keys[i] = Util.uuid();
            values[i] = Util.uuid();
        }


        final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        final CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_ITERATIONS);


        //First iterations are for warm-up and are not counted;
        //Anything else are counted and aggregated as a one result;
        for (int iteration = 0; iteration < NUMBER_OF_ITERATIONS; iteration++) {

            final int localIteration = iteration;
            final Runnable runnable =
                    () -> {

                        for (int puts = MAP_SIZE[0]; puts < MAP_SIZE[1]; puts++) {
                            for (int tests = TEST[0]; tests < TEST[1]; tests++) {
                                final int allTests = TEST_STEPS * tests;

                                Map[] holder1 = new Map[allTests];

                                //Start working with HashMap
                                final long hashMapRes;
                                long start = System.nanoTime();
                                for (int i = 0; i < allTests; i++) {
                                    final Map<String, String> stringStringMap = new HashMap<>();
                                    for (int j = 0; j < puts; j++) {
                                        stringStringMap.put(keys[j], values[j]);
                                    }
                                    holder1[i] = stringStringMap;
                                }
                                long end = System.nanoTime();
                                hashMapRes = end - start;
                                //End working with HashMap

                                holder1 = null;

                                Map[] holder2 = new Map[allTests];

                                //Start working with ArrayMap
                                final long arrayMapRes;
                                start = System.nanoTime();
                                for (int i = 0; i < allTests; i++) {
                                    final Map<String, String> stringStringMap = new ArrayMap<>();
                                    for (int j = 0; j < puts; j++) {
                                        stringStringMap.put(keys[j], values[j]);
                                    }
                                    holder2[i] = stringStringMap;
                                }
                                end = System.nanoTime();

                                arrayMapRes = end - start;
                                //End working with ArrayMap
                                holder2 = null;

//                    if (Math.abs(hashMapRes - arrayMapRes) < 50) {
//                        resultsMemo[puts][tests][k] = '\0';
//                    } else {
                                resultsMemo[puts][tests][localIteration] = hashMapRes > arrayMapRes ? 'A' : 'H';
//                    }
                                synchronized (ManualTests.class) {
                                    System.out.printf("hashMapRes = %s and arrayMapRes = %s => winner %c\n",
                                            hashMapRes,
                                            arrayMapRes,
                                            resultsMemo[puts][tests][localIteration]
                                    );
                                }
                            }

                        }
                        countDownLatch.countDown();
                    };
            executorService.submit(runnable);
        }
        countDownLatch.await();
        executorService.shutdown();

        final XSSFWorkbook workbook = new XSSFWorkbook();
        final String rangeForConditionalFormatting = Util.getGridRange(TEST[1] + 1, MAP_SIZE[1] + 1);
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
            for (int puts = MAP_SIZE[0]; puts < MAP_SIZE[1]; puts++) {
                Row row = sheet.createRow(++rowCount);
                row.createCell(0).setCellValue(puts);
                int columnCount = 0;
                for (int tests = TEST[0]; tests < TEST[1]; tests++) {
                    char res;
                    if (iteration == NUMBER_OF_ITERATIONS) {
                        res = Util.getMostRepeatingCharacter(Arrays.copyOfRange(resultsMemo[puts][tests], WARN_UP_ITERATIONS, NUMBER_OF_ITERATIONS));
                    } else {
                        res = resultsMemo[puts][tests][iteration];
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
                    CellRangeAddress.valueOf(rangeForConditionalFormatting)
            };
            sheetCF.addConditionalFormatting(regions, rule1, rule2);

        }

        try (FileOutputStream outputStream = new FileOutputStream("HashMap and ArrayMap heat map. From " + new Date().getTime() + ".xlsx")) {
            workbook.write(outputStream);
        }

        System.out.printf("Time needed in min: %f%n", (System.currentTimeMillis() - globalStart) / (60.0 * 1000));
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