package ui.settings;

import checker.filter.cache.FilterCache;
import cn.hutool.json.JSONUtil;
import common.logger.AutoSSRFLogger;
import common.pool.CollaboratorThreadPool;
import lombok.Data;
import scanner.SSRFHttpHandler;

import java.io.*;

@Data
public class Settings {
    private static final String settingsFilePath = System.getProperty("user.home") +
            "\\AppData\\Roaming\\BurpSuite\\AutoSSRFConfig.json";
    private static final CollaboratorThreadPool collaboratorThreadPool = CollaboratorThreadPool.INSTANCE;
    private static final AutoSSRFLogger logger = AutoSSRFLogger.INSTANCE;
    private static final Settings initSettings = Settings.exportAutoSSRFExtensionSettingAsJson();
    private Extensions extensions = new Extensions();
    private ThreadPool threadPool = new ThreadPool();
    private Cache cache = new Cache();
    private static Settings INSTANCE;

    public static Settings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = exportAutoSSRFExtensionSettingAsJson();
            if (INSTANCE == null) {
                INSTANCE = new Settings();
            }
        }
        return INSTANCE;
    }
    @Data
    public static class Extensions {
        private boolean proxy;
        private String payload;


        public Extensions() {
            if (initSettings == null) {
            this.proxy = SSRFHttpHandler.isProxyEnabled();
                this.payload= null;
                return;
            }

            Extensions initExtensions = initSettings.getExtensions();
            this.proxy = initExtensions.isProxy();
            this.payload=initExtensions.getPayload();
        }


        public void setProxyReal(boolean proxy) {
            SSRFHttpHandler.setProxyEnabled(proxy);
            setProxy(proxy);
        }

        public void setPayload(String payload) {
            this.payload = payload;
//            logger.logToOutput("设置成功"+payload);
        }

        public String getPayload() {
            return this.payload;
        }
    }

    @Data
    public static class ThreadPool {
        private int corePoolSize;
        private int maxPoolSize;

        public ThreadPool() {
            if (initSettings == null) {
                this.corePoolSize = collaboratorThreadPool.getCorePoolSize();
                this.maxPoolSize = collaboratorThreadPool.getMaxPoolSize();
                return;
            }

            ThreadPool initThreadPool = initSettings.getThreadPool();
            this.corePoolSize = initThreadPool.corePoolSize;
            this.maxPoolSize = initThreadPool.maxPoolSize;
        }

        public void setPoolSize(int corePoolSize, int maxPoolSize) {
            collaboratorThreadPool.setPoolSize(corePoolSize, maxPoolSize);
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
        }
    }

    @Data
    public static class Cache {
        private String cacheFilePath;
        private String cacheFileName;

        public Cache() {
            if (initSettings == null) {
                File cacheFile = new File(FilterCache.getPath());
                this.cacheFilePath = cacheFile.getParentFile().getAbsolutePath();
                this.cacheFileName = cacheFile.getName();
                return;
            }

            Cache initCache = initSettings.getCache();
            this.cacheFilePath = initCache.getCacheFilePath();
            this.cacheFileName = initCache.getCacheFileName();
        }

        public String getCacheFilePathHaveName() {
            return cacheFilePath + "\\" + cacheFileName;
        }
    }

    public static Settings exportAutoSSRFExtensionSettingAsJson() {
        try {
            File configFile = new File(settingsFilePath);
            if (!configFile.exists()) {
                return null;
            }

            try (FileReader fileReader = new FileReader(configFile)) {
                try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                    StringBuilder settingsString = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        settingsString.append(line);
                    }
                    return JSONUtil.toBean(settingsString.toString(), Settings.class);
                }
            }
        } catch (Exception e) {
            logger.logToError(e);
        }
        return null;
    }

    public static void saveAutoSSRFExtensionSetting(Settings settings) {
        try {
            File configFile = new File(settingsFilePath);
            if (configFile.exists()) {
                boolean delete = configFile.delete();
                if (!delete) {
                    throw new RuntimeException("删除文件失败: " + configFile.getAbsolutePath());
                }
            }

            File parentFile = configFile.getParentFile();
            if (!parentFile.exists()) {
                boolean success = parentFile.mkdirs();
                if (!success) {
                    throw new RuntimeException("创建文件夹失败: " + parentFile.getAbsolutePath());
                }
            }

            try (FileWriter fileWriter = new FileWriter(configFile)) {
                try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                    bufferedWriter.append(JSONUtil.toJsonPrettyStr(settings));
                    bufferedWriter.flush();
                }
            }
        } catch (Exception e) {
            logger.logToError(e);
        }
    }
}
