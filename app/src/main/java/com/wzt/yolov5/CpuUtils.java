package com.wzt.yolov5;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class CpuUtils {
/*
        private CpuUtils() {
            //no instance
        }
        private static final List<String> CPU_TEMP_FILE_PATHS = Arrays.asList(
                "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
                "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
                "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
                "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
                "/sys/devices/platform/tegra_tmon/temp1_input",
                "/sys/kernel/debug/tegra_thermal/temp_tj",
                "/sys/devices/platform/s5p-tmu/temperature",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/class/hwmon/hwmon0/device/temp1_input",
                "/sys/devices/virtual/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone3/temp",
                "/sys/class/thermal/thermal_zone4/temp",
                "/sys/class/hwmon/hwmonX/temp1_input",
                "/sys/devices/platform/s5p-tmu/curr_temp");

        public static final Maybe<CpuTemperatureResult> getCpuTemperatureFinder() {
            return Observable
                    .fromIterable(CPU_TEMP_FILE_PATHS)
                    .map(new Function<String, CpuTemperatureResult>() {
                        @Override
                        public CpuTemperatureResult apply(String path) {
                            Double temp = readOneLine(new File(path));
                            String validPath = "";
                            double currentTemp = 0.0D;
                            if (isTemperatureValid(temp)) {
                                validPath = path;
                                currentTemp = temp;
                            } else if (isTemperatureValid(temp / (double) 1000)) {
                                validPath = path;
                                currentTemp = temp / (double) 1000;
                            }

                            return new CpuTemperatureResult(validPath, (int) currentTemp);
                        }
                    }).filter(new Predicate<CpuTemperatureResult>() {
                        @Override
                        public boolean test(CpuTemperatureResult cpuTemperatureResult) throws Exception {
                            return !TextUtils.isEmpty(cpuTemperatureResult.getFilePath())
                                    && (cpuTemperatureResult.getTemp() != 0);
                        }
                    }).firstElement();
        }

        private static double readOneLine(File file) {
            FileInputStream fileInputStream = null;
            String s = "";
            try {
                fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                s = bufferedReader.readLine();
                fileInputStream.close();
                inputStreamReader.close();
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            double result = 0;
            try {
                result = Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
            }
            return result;
        }

        private static boolean isTemperatureValid(double temp) {
            return temp >= -30.0D && temp <= 250.0D;
        }
*/
}
