package checker;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import checker.filter.RequestResponseFilter;
import checker.updater.ParamsUpdater;
import cn.hutool.core.util.RandomUtil;
import common.logger.AutoSSRFLogger;
import common.provider.CollaboratorProvider;
import common.provider.UIProvider;
import common.provider.HttpProvider;
import lombok.Getter;
import ui.dashboard.DashboardTable;
import ui.dashboard.DashboardTableData;
import ui.settings.Settings;

import java.security.cert.CertificateRevokedException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public enum SSRFChecker {
    INSTANCE;

    private final AutoSSRFLogger logger = AutoSSRFLogger.INSTANCE;
    private final CollaboratorProvider collaboratorProvider = CollaboratorProvider.INSTANCE;
    private final DashboardTable dashboardTable = UIProvider.INSTANCE.getUiMain().getDashboardTab().getTable();

    @Getter
    private final RequestResponseFilter filter = new RequestResponseFilter();
    private final ParamsUpdater updater = new ParamsUpdater();

    public void check(HttpRequestResponse baseRequestResponse, Integer id) {
        //HttpRequestResponse,从burp历史中获取数据包
        HttpRequest request = baseRequestResponse.request();

        if (!filter.filter(baseRequestResponse, id)) {
//            logger.logToOutput("\n过滤逻辑执行\n");
            return;
        }

        Settings settings = new Settings();
        String payload=settings.getInstance().getExtensions().getPayload();
//        logger.logToOutput("\npayload:\n"+payload);

        //自定义paylaod，待会从控制台获取
//               String payload = "test.com/xxx";

        // 参数填充&构建请求
        HttpRequest newRequest = updateParameterAndBuildRequest(
                request,
                filter.getParameters(),
                payload
        );
        HttpRequestResponse newRequestResponse = HttpProvider.INSTANCE.sendRequest(newRequest);


        //更新 Dashboard 表格（可视化界面）
        if (id == null) {
            // 防止 并发 生成重复ID
            id = dashboardTable.generateId();
            dashboardTable.addRow(DashboardTableData.buildDashboardTableData(
                    id,
                    baseRequestResponse,
                   newRequestResponse
            ));
        }
    }

    //构造注入了payload的请求
    private HttpRequest updateParameterAndBuildRequest(
            HttpRequest request,
            List<ParsedHttpParameter> parameters,
            String payloadBaseUrl
    ) {
        List<HttpParameter> updateParameters = new ArrayList<>();

        for (HttpParameter parameter : parameters) {
            String rawValue = parameter.value();

            int decodeCount = getUrlEncodeLevel(rawValue);

            // 构造 token 和 payload
            String token = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "_" + RandomUtil.randomString(3);

            // 保留原始结构，仅替换为 payload
            String urlpayload = generatePayload(rawValue, payloadBaseUrl, token, decodeCount);

            updateParameters.add(HttpParameter.parameter(
                    parameter.name(),
                    urlpayload,
                    parameter.type()
            ));

        }

        return updater.update(request, updateParameters);
    }

    private int getUrlEncodeLevel(String url) {
        // 编码三次的匹配：http%25253A%25252F%25252F 或 https...
        Pattern level3 = Pattern.compile("https?%25253A%25252F%25252F", Pattern.CASE_INSENSITIVE);
        // 编码两次：http%253A%252F%252F
        Pattern level2 = Pattern.compile("https?%253A%252F%252F", Pattern.CASE_INSENSITIVE);
        // 编码一次：http%3A%2F%2F
        Pattern level1 = Pattern.compile("https?%3A%2F%2F", Pattern.CASE_INSENSITIVE);
        if (level3.matcher(url).find()) {
            return 3;
        } else if (level2.matcher(url).find()) {
            return 2;
        } else if (level1.matcher(url).find()) {
            return 1;
        } else  {
            return 0;
        }
    }


    // 根据编码次数生成 payload
    private String generatePayload(String rawValue, String payloadBaseUrl, String token, int encodeCount) {
        String payloadToInsert =payloadBaseUrl.contains("?") ? payloadBaseUrl+"&": payloadBaseUrl+ "?token="+token+"&";

        try {
            for (int i = 0; i < encodeCount; i++) {
                payloadToInsert = java.net.URLEncoder.encode(payloadToInsert, "UTF-8");
            }
        } catch (Exception e) {
            logger.logToOutput("Payload 编码失败: " + e.getMessage());
            return rawValue;
        }

        // 匹配协议头
        String protocolEncoded;
        switch (encodeCount) {
            case 3:
                protocolEncoded = "%25253A%25252F%25252F";
                break;
            case 2:
                protocolEncoded = "%253A%252F%252F";
                break;
            case 1:
                protocolEncoded = "%3A%2F%2F";
                break;
            default:
                protocolEncoded = "://";
                break;
        }

        // 分割原始字符串以处理所有匹配项
        String[] parts = rawValue.split(java.util.regex.Pattern.quote(protocolEncoded), -1);
        if (parts.length <= 1) {
            logger.logToOutput("未匹配到协议头，使用默认方式返回");
            return rawValue;
        }

        // 在每个协议头后插入payload
        String replacement = protocolEncoded + payloadToInsert;
        StringBuilder resultBuilder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            resultBuilder.append(parts[i]);
            if (i < parts.length - 1) {
                resultBuilder.append(replacement);
            }
        }

        String result = resultBuilder.toString();
        logger.logToOutput("最终拼接结果:\n" + result);
        return result;
    }






}

