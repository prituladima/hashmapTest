package com.prituladima.benchmarks;

import androidx.collection.ArrayMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.poi.ss.usermodel.ComparisonOperator.EQUAL;


public class ReportGenerator {

    public static void main(String[] args) throws IOException, InterruptedException {

        final long globalStart = System.currentTimeMillis();

        final int cores = Runtime.getRuntime().availableProcessors();

        final int[] SIZES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        final int maxSize = Arrays.stream(SIZES).max().getAsInt() + 1;

        final int TESTS = 10_000;

        final int baseAllCoresIterations = 10_000_000 * 3 / 16;
        final int allCoresIterations = TESTS * SIZES.length / cores;

        final double baseExpectedTime = 7.286;
        final double expected_time = baseExpectedTime * allCoresIterations / baseAllCoresIterations;

        System.out.printf("Expected time: %f min. Do you want to continue? Y/N: ", expected_time);
        if (!"Y".equalsIgnoreCase(new Scanner(System.in).next())) {
            System.exit(0);
        }

        final int WARN_UP_ITERATIONS = 0;
        final int NUMBER_OF_ITERATIONS = 10;

        final char[][] resultsMemo = new char[maxSize][NUMBER_OF_ITERATIONS];

        //Pre-generate data
        final String[] keys = new String[maxSize];
        final String[] values = new String[maxSize];
        for (int i = 0; i < maxSize; i++) {
            keys[i] = Util.uuid();
            values[i] = Util.uuid();
        }


        final ExecutorService executorService = Executors.newFixedThreadPool(cores);
        final CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_ITERATIONS);


        //First iterations are for warm-up and are not counted;
        //Anything else are counted and aggregated as a one result;
        for (int iteration = 0; iteration < NUMBER_OF_ITERATIONS; iteration++) {

            final int localIteration = iteration;
            final Runnable runnable =
                    () -> {

                        for (final int site : SIZES) {
                            int amountOfTests = TESTS / Math.max(site, 1);

                            final long hashMapRes;
                            {
//                                Map[] holder1 = new Map[amountOfTests];

                                //Start working with HashMap
                                long start = System.currentTimeMillis();
                                for (int i = 0; i < amountOfTests; i++) {
                                    final Map<String, String> stringStringMap = new HashMap<>();
                                    for (int j = 0; j < site; j++) {
                                        stringStringMap.put(keys[j], values[j]);
                                    }
                                    blackHole(stringStringMap);
                                }
                                long end = System.currentTimeMillis();
                                hashMapRes = end - start;
                                //End working with HashMap


                            }
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////
                            final long arrayMapRes;
                            {
//                                Map[] holder2 = new Map[amountOfTests];

                                //Start working with ArrayMap
                                long start = System.currentTimeMillis();
                                for (int i = 0; i < amountOfTests; i++) {
                                    final Map<String, String> stringStringMap = new ArrayMap<>();
                                    for (int j = 0; j < site; j++) {
                                        stringStringMap.put(keys[j], values[j]);
                                    }
                                    blackHole(stringStringMap);
                                }
                                long end = System.currentTimeMillis();

                                arrayMapRes = end - start;
                                //End working with ArrayMap

                            }
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////

                            final long treeMapRes;
                            {
//                                Map[] holder1 = new Map[amountOfTests];

                                //Start working with TreeMap
                                long start = System.currentTimeMillis();
                                for (int i = 0; i < amountOfTests; i++) {
                                    final Map<String, String> stringStringMap = new TreeMap<>();
                                    for (int j = 0; j < site; j++) {
                                        stringStringMap.put(keys[j], values[j]);
                                    }
                                    blackHole(stringStringMap);
                                }
                                long end = System.currentTimeMillis();
                                treeMapRes = end - start;
                                //End working with TreeMap

                            }
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////
                            ////////////////////////////////////////////

                            if (hashMapRes == arrayMapRes && hashMapRes == treeMapRes) {
                                resultsMemo[site][localIteration] = '\0';
                            } else if (hashMapRes <= arrayMapRes && hashMapRes <= treeMapRes) {
                                resultsMemo[site][localIteration] = 'H';
                            } else if (arrayMapRes <= hashMapRes && arrayMapRes <= treeMapRes) {
                                resultsMemo[site][localIteration] = 'A';
                            } else if (treeMapRes <= hashMapRes && treeMapRes <= arrayMapRes) {
                                resultsMemo[site][localIteration] = 'T';
                            }

//                            synchronized (ManualTests.class) {
//                                System.out.printf("size = %s hashMapRes = %s and arrayMapRes = %s => winner %c\n",
//                                        site,
//                                        hashMapRes,
//                                        arrayMapRes,
//                                        resultsMemo[site][localIteration]
//                                );
//                            }

                        }
                        countDownLatch.countDown();
//                        synchronized (ManualTests.class) {
//                            System.out.println(countDownLatch.getCount());
//                        }
                    };
            executorService.submit(runnable);
        }
        countDownLatch.await();
        executorService.shutdown();

        final XSSFWorkbook workbook = new XSSFWorkbook();
        final XSSFSheet sheet = workbook.createSheet("Aggregated");

        {
            final XSSFRow rowWithSized = sheet.createRow(0);
            int pointer = 1;
            for (int size : SIZES) {
//                final String colName = String.format("%s #%d.", iteration < WARN_UP_ITERATIONS ? "Warm up" : "Iteration", iteration);
                rowWithSized
                        .createCell(pointer++)
                        .setCellValue(String.format("Size #%d.", size));
            }
        }

        for (int iteration = 1; iteration <= NUMBER_OF_ITERATIONS; iteration++) {

            final XSSFRow rowWithResults = sheet.createRow(iteration);

            rowWithResults.createCell(0).setCellValue(String.format("It: #%d", iteration));

            int colPointer = 1;
            for (final int site : SIZES) {
                char res = resultsMemo[site][iteration - 1];
                rowWithResults.createCell(colPointer++).setCellValue(String.valueOf(res));
            }
        }

        {

            final XSSFRow rowWithResults = sheet.createRow(NUMBER_OF_ITERATIONS + 1);

            rowWithResults.createCell(0).setCellValue("Res:");

            int colPointer = 1;
            for (final int site : SIZES) {
                char res = Util.getMostRepeatingCharacter(Arrays.copyOfRange(resultsMemo[site], WARN_UP_ITERATIONS, NUMBER_OF_ITERATIONS));
                rowWithResults.createCell(colPointer++).setCellValue(String.valueOf(res));
            }

        }

        final String rangeForConditionalFormatting = Util.getGridRange(SIZES.length + 1, NUMBER_OF_ITERATIONS + 2);

        {
            SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

            ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(EQUAL, "\"H\"");
            PatternFormatting fill1 = rule1.createPatternFormatting();
            fill1.setFillBackgroundColor(IndexedColors.RED.index);
            fill1.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

            ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(EQUAL, "\"A\"");
            PatternFormatting fill2 = rule2.createPatternFormatting();
            fill2.setFillBackgroundColor(IndexedColors.GREEN.index);
            fill2.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

            ConditionalFormattingRule rule3 = sheetCF.createConditionalFormattingRule(EQUAL, "\"T\"");
            PatternFormatting fill3 = rule3.createPatternFormatting();
            fill3.setFillBackgroundColor(IndexedColors.BLUE.index);
            fill3.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

            final CellRangeAddress[] regions = {
                    CellRangeAddress.valueOf(rangeForConditionalFormatting)
            };
            sheetCF.addConditionalFormatting(regions, new ConditionalFormattingRule[]{rule1, rule2, rule3});
        }

        try (FileOutputStream outputStream = new FileOutputStream("HashMap and ArrayMap heat map. From " + new Date().getTime() + ".xlsx")) {
            workbook.write(outputStream);
        }

        System.out.printf("Time needed in min: %f%n", (System.currentTimeMillis() - globalStart) / (60.0 * 1000));
    }

    private static void blackHole(Map<?, ?> map) {

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