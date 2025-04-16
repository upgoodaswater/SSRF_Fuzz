package ui.settings;

import checker.SSRFChecker;
import checker.filter.cache.FilterCache;
import common.logger.AutoSSRFLogger;
import common.provider.UIProvider;
//import scanner.SSRFHttpHandler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.util.function.Consumer;

public class SettingsTab extends JPanel {

    private final UIProvider uiProvider = UIProvider.INSTANCE;
    private final Settings settings = new Settings();

    public SettingsTab() {
        this.setLayout(null);

        int rootX = 0;
        int rootY = 0;

        // 插件配置
        Component extensionsSettingUIEndComponent = createExtensionsSettingUI(rootX, rootY);

        // 线程池配置
        Component threadPoolSettingUIEndComponent = createThreadPoolSettingUI(
                extensionsSettingUIEndComponent.getX(),
                extensionsSettingUIEndComponent.getY() + extensionsSettingUIEndComponent.getHeight() + 20 // 增加间距
        );

        // 缓存配置
        Component cacheSettingUIEndComponent = createCacheSettingUI(
                threadPoolSettingUIEndComponent.getX(),
                threadPoolSettingUIEndComponent.getY() + threadPoolSettingUIEndComponent.getHeight()
        );

    }

    private Component createExtensionsSettingUI(int rootX, int rootY) {
        Font burpFont = uiProvider.currentDisplayFont();
        Settings.Extensions extensions = settings.getInstance().getExtensions();

        extensions.setProxyReal(false);
        //SSRFHttpHandler.setProxyEnabled(false);

        JLabel globalSettingLabel = new JLabel("插件配置");
        globalSettingLabel.setFont(createBolderFont(burpFont));
        globalSettingLabel.setBounds(
                rootX + 20,
                rootY + 20,
                200,
                20
        );

        // 代理开关按钮
        JLabel payloadLabel = new JLabel("Payload:");
        payloadLabel.setFont(createNormalFont(burpFont));
        payloadLabel.setBounds(
                globalSettingLabel.getX(),
                globalSettingLabel.getY() + globalSettingLabel.getHeight() + 10,
                60,
                20
        );

        // Payload输入组件
        JTextField payloadInput = new JTextField();
        payloadInput.setBounds(
                payloadLabel.getX() + payloadLabel.getWidth() + 5,
                payloadLabel.getY(),
                250,
                20
        );
        payloadInput.setText(extensions.getPayload());

        // 设置是否允许编辑（根据当前是否启用代理）
        payloadInput.setEditable(!extensions.isProxy());

        // 代理切换按钮
        JButton proxyToggleButton = new JButton();
        proxyToggleButton.setBounds(
                globalSettingLabel.getX(),
                payloadInput.getY() + payloadInput.getHeight() + 15,
                80,
                20
        );

        updateProxyButtonState(proxyToggleButton, extensions.isProxy());

        proxyToggleButton.addActionListener(e -> {
            boolean newState = !extensions.isProxy();
            extensions.setProxyReal(newState);
            //SSRFHttpHandler.setProxyEnabled(newState);
            updateProxyButtonState(proxyToggleButton, newState);
            extensions.setPayload(payloadInput.getText());

            // 禁用或启用 payload 编辑
            payloadInput.setEditable(!newState);

            Settings.saveAutoSSRFExtensionSetting(settings);

        });
        this.add(globalSettingLabel);
        this.add(proxyToggleButton);
        this.add(payloadLabel);
        this.add(payloadInput);

        return proxyToggleButton;
    }


    // 更新按钮状态的方法
    private void updateProxyButtonState(JButton button, boolean isEnabled) {
        button.setText(isEnabled ? "STOP" : "START");
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);

    }


    private Component createThreadPoolSettingUI(int rootX, int rootY) {
        Font burpFont = uiProvider.currentDisplayFont();
        Settings.ThreadPool threadPool = settings.getThreadPool();

        JLabel threadPoolSettingLabel = new JLabel("线程池配置");
        threadPoolSettingLabel.setFont(createBolderFont(burpFont));
        threadPoolSettingLabel.setBounds(
                rootX,
                rootY + 15,
                200,
                20
        );

        // 核心线程数输入框
        JLabel corePoolSizeLabel = new JLabel("核心线程数:");
        setInputFontAndBounds(threadPoolSettingLabel, corePoolSizeLabel);
        JTextField corePoolSizeInput = new JTextField();
        corePoolSizeInput.setBounds(
                corePoolSizeLabel.getX() + corePoolSizeLabel.getWidth() + 5,
                corePoolSizeLabel.getY(),
                200, 20
        );
        corePoolSizeInput.setText(String.valueOf(threadPool.getCorePoolSize()));

        // 最大线程数输入框
        JLabel maxPoolSizeLabel = new JLabel("最大线程数:");
        setInputFontAndBounds(corePoolSizeLabel, maxPoolSizeLabel);
        JTextField maxPoolSizeInput = new JTextField();
        maxPoolSizeInput.setBounds(
                maxPoolSizeLabel.getX() + maxPoolSizeLabel.getWidth() + 5,
                maxPoolSizeLabel.getY(),
                200, 20
        );
        maxPoolSizeInput.setText(String.valueOf(threadPool.getMaxPoolSize()));

        // 用 DocumentListener 实时生效
        DocumentListener applyOnChange = new DocumentListener() {
            private void update() {
                try {
                    int core = Integer.parseInt(corePoolSizeInput.getText());
                    int max  = Integer.parseInt(maxPoolSizeInput.getText());
                    threadPool.setPoolSize(core, max);
                } catch (NumberFormatException ignore) {
                    // 输入不全或非数字时，暂不处理
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        };
        corePoolSizeInput.getDocument().addDocumentListener(applyOnChange);
        maxPoolSizeInput.getDocument().addDocumentListener(applyOnChange);

        // 如果你不需要“生效”按钮，可以不创建它；这里只留空返回一个组件占位
        JPanel placeholder = new JPanel();
        placeholder.setBounds(
                maxPoolSizeLabel.getX(),
                maxPoolSizeLabel.getY() + maxPoolSizeInput.getHeight() + 5,
                0, 0
        );

        this.add(threadPoolSettingLabel);
        this.add(corePoolSizeLabel);
        this.add(corePoolSizeInput);
        this.add(maxPoolSizeLabel);
        this.add(maxPoolSizeInput);
        // 不再添加 saveButton

        return placeholder;
    }




    private Component createCacheSettingUI(int rootX, int rootY) {
        Font burpFont = uiProvider.currentDisplayFont();
        Settings.Cache cacheSetting = settings.getCache();

        JLabel cacheSettingLabel = new JLabel("缓存配置");
        cacheSettingLabel.setFont(createBolderFont(burpFont));
        cacheSettingLabel.setBounds(
                rootX,
                rootY + 15,
                200,
                20
        );

        // 缓存配置
        JLabel cacheFilePathChooserLabel = new JLabel("缓存文件路径:");
        cacheFilePathChooserLabel.setFont(createNormalFont(burpFont));
        cacheFilePathChooserLabel.setBounds(
                cacheSettingLabel.getX(),
                cacheSettingLabel.getY() + cacheSettingLabel.getHeight() + 5,
                80, 20
        );

        JTextField cacheFilePathInput = new JTextField();
        cacheFilePathInput.setBounds(
                cacheFilePathChooserLabel.getX() + cacheFilePathChooserLabel.getWidth() + 5,
                cacheFilePathChooserLabel.getY(),
                200, 20
        );
        cacheFilePathInput.setText(cacheSetting.getCacheFilePath());
        cacheFilePathInput.getDocument().addDocumentListener(
                buildDocumentListener(e -> cacheSetting.setCacheFilePath(cacheFilePathInput.getText()))
        );

        JButton fileChooserButton = new JButton("选择文件夹");
        fileChooserButton.setBounds(
                cacheFilePathInput.getX() + cacheFilePathInput.getWidth() + 5,
                cacheFilePathInput.getY(),
                90, 20
        );
        fileChooserButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.showDialog(null, "确认");
            cacheFilePathInput.setText(fileChooser.getSelectedFile().getAbsolutePath());
        });

        JLabel cacheFileNameLabel = new JLabel("缓存文件名:");
        cacheFileNameLabel.setFont(createNormalFont(burpFont));
        cacheFileNameLabel.setBounds(
                cacheFilePathChooserLabel.getX(),
                cacheFilePathChooserLabel.getY() + cacheFilePathChooserLabel.getHeight() + 5,
                80, 20
        );

        JTextField cacheFileNameInput = new JTextField();
        cacheFileNameInput.setBounds(
                cacheFileNameLabel.getX() + cacheFileNameLabel.getWidth() + 5,
                cacheFileNameLabel.getY(),
                200, 20
        );
        cacheFileNameInput.setText(cacheSetting.getCacheFileName());
        cacheFileNameInput.getDocument().addDocumentListener(
                buildDocumentListener(e -> cacheSetting.setCacheFileName(cacheFileNameInput.getText()))
        );

        JLabel cacheObjCountLabel = new JLabel("缓存对象数:");
        cacheObjCountLabel.setFont(createNormalFont(burpFont));
        cacheObjCountLabel.setBounds(
                cacheFileNameLabel.getX(),
                cacheFileNameLabel.getY() + cacheFileNameLabel.getHeight() + 5,
                80, 20
        );

        JTextField cacheObjCountInput = new JTextField();
        cacheObjCountInput.setBounds(
                cacheObjCountLabel.getX() + cacheObjCountLabel.getWidth() + 5,
                cacheObjCountLabel.getY(),
                200, 20
        );
        cacheObjCountInput.setText("点击下方按钮获取");
        cacheObjCountInput.setEditable(false);
        cacheObjCountInput.setFocusable(false);

        JButton cacheCountButton = new JButton("刷新缓存数");
        cacheCountButton.setBounds(
                cacheObjCountLabel.getX(),
                cacheObjCountLabel.getY() + cacheObjCountLabel.getHeight() + 5,
                90, 20
        );
        cacheCountButton.addActionListener(e -> {
            FilterCache<String, Byte> cache = SSRFChecker.INSTANCE.getFilter().getCache();
            Integer cacheCount = cache.getCacheCount();
            cacheObjCountInput.setText(String.valueOf(cacheCount));
        });

        JButton clearCacheFileButton = new JButton("清理缓存");
        setButtonBounds(cacheCountButton, clearCacheFileButton);
        clearCacheFileButton.addActionListener(e -> {
            FilterCache<String, Byte> cache = SSRFChecker.INSTANCE.getFilter().getCache();
            cache.clear();
        });

        JButton deleteCacheFileButton = new JButton("删除缓存文件");
        setButtonBounds(clearCacheFileButton, deleteCacheFileButton);
        deleteCacheFileButton.addActionListener(e -> {
            FilterCache<String, Byte> cache = SSRFChecker.INSTANCE.getFilter().getCache();
            cache.delete();
        });

        JButton saveCacheFileButton = new JButton("保存缓存文件");
        setButtonBounds(deleteCacheFileButton, saveCacheFileButton);
        saveCacheFileButton.addActionListener(e -> {
            FilterCache<String, Byte> cache = SSRFChecker.INSTANCE.getFilter().getCache();
            try {
                FilterCache.setPath(cacheSetting.getCacheFilePathHaveName());
                cache.store();
            } catch (IOException exception) {
                AutoSSRFLogger.INSTANCE.logToError(exception);
            }
        });

        this.add(cacheSettingLabel);
        this.add(cacheFilePathChooserLabel);
        this.add(cacheFilePathInput);
        this.add(fileChooserButton);
        this.add(cacheFileNameLabel);
        this.add(cacheObjCountLabel);
        this.add(cacheObjCountInput);
        this.add(cacheCountButton);
        this.add(cacheFileNameInput);
        this.add(clearCacheFileButton);
        this.add(deleteCacheFileButton);
        this.add(saveCacheFileButton);
        return cacheCountButton;
    }

//    private void saveAllSettingUI(int rootX, int rootY) {
//        JButton saveAllSettingButton = new JButton("保存配置");
//        saveAllSettingButton.setBounds(
//                rootX,
//                rootY + 15,
//                80, 20
//        );
//        saveAllSettingButton.addActionListener(e -> Settings.saveAutoSSRFExtensionSetting(settings));
//        this.add(saveAllSettingButton);
//    }

    private void setButtonBounds(Component positionComponent, Component currentComponent) {
        currentComponent.setBounds(
                positionComponent.getX() + positionComponent.getWidth() + 3,
                positionComponent.getY(),
                100, 20
        );
    }

    private void setInputFontAndBounds(Component positionComponent, Component currentComponent) {
        Font burpFont = uiProvider.currentDisplayFont();
        currentComponent.setFont(createNormalFont(burpFont));
        currentComponent.setBounds(
                positionComponent.getX(),
                positionComponent.getY() + positionComponent.getHeight() + 5,
                70, 20
        );
    }

    private void setCheckboxFontAndBounds(Component positionComponent, Component currentComponent) {
        Font burpFont = uiProvider.currentDisplayFont();
        currentComponent.setFont(createNormalFont(burpFont));
        currentComponent.setBounds(
                positionComponent.getX() + positionComponent.getWidth() + 3,
                positionComponent.getY(),
                100, 20
        );
    }

    private Font createBolderFont(Font burpFont) {
        return new Font(burpFont.getName(), Font.PLAIN, burpFont.getSize() + 7);
    }

    private Font createNormalFont(Font burpFont) {
        return new Font(burpFont.getName(), Font.PLAIN, burpFont.getSize() + 1);
    }

    private DocumentListener buildDocumentListener(Consumer<DocumentEvent> function) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                function.accept(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                function.accept(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                function.accept(e);
            }
        };
    }
}
