package eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield;

import static eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam.*;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam;

import clover.com.google.common.collect.BiMap;
import clover.com.google.common.collect.ImmutableBiMap;

public class GlobalConfigurationApplicationProfileFields {

    public static final String MESSAGING_SETTINGS = "messagingSettings";
    public static final String BUSINESS_KEY_ALIAS_PASSWORD_LIST = "businessKeyAliasPasswordList";
    public static final String CLUSTER_NODES = "clusterNodes";

    public static final String CERTIFICATE_ALIAS_PASSWORD = "certificateAliasPassword";
    public static final String CERTIFICATE_ALIAS = "certificateAlias";

    public static final String PMODE_PATH = "pmodePath";
    public static final String NAME = "name";

    public static final String PRIVATE_KEYSTORE_PASSWORD = "privateKeystorePassword";
    public static final String XSD_REPOSITORY_PATH = "xsdRepositoryPath";
    public static final String USER_COMPRESSION = "useCompression";
    public static final String OPERATION_MODE = "operationMode";
    public static final String USE_PULL_MODE = "usePullMode";
    public static final String DEFAULT_PULLING_INTERVAL = "defaultPullingInterval";
    public static final String ANTIMALWARE_MODE = "antimalwareMode";
    public static final String ANTIMALWARE_SETTINGS = "antimalwareSettings";
    public static final String ANTIMALWARE_INFECTED_SETTINGS = "antimalwareInfectedSettings";
    public static final String BUSINESS_KEYSTORE_PASSWORD = "businessKeystorePassword";
    public static final String BMP_VALIDATION_MODE = "bmpValidationMode";
    public static final String MAX_MESSAGE_SIZE = "maxMessageSize";
    public static final String MSH_PATH = "mshPath";
    public static final String PUBLIC_KEYSTORE_PASSWORD = "publicKeystorePassword";
    public static final String TLS_TRUSTSTORE_PASSWORD = "tlsTruststorePassword";
    public static final String MAX_RETRIES = "maxRetries";
    public static final String TLS_KEYSTORE_PASSWORD = "tlsKeystorePassword";
    public static final String RETRY_INTERVAL = "retryInterval";

    public static final BiMap<String, EGlobalParam> GLOBAL_PARAM_MAP = new ImmutableBiMap.Builder<String, EGlobalParam>()
            .put(BMP_VALIDATION_MODE, EGlobalParam.MESSAGING_BMP_VALIDATION_MODE)
            .put(XSD_REPOSITORY_PATH, EGlobalParam.MESSAGING_REPOSITORY_XSD_PATH)
            .put(MSH_PATH, EGlobalParam.MESSAGING_MSH_PATH)

            .put(PRIVATE_KEYSTORE_PASSWORD, EGlobalParam.MESSAGING_SECURITY_EBMS_PRIVATE_KEYSTORE_PASSWORD)
            .put(PUBLIC_KEYSTORE_PASSWORD, EGlobalParam.MESSAGING_SECURITY_EBMS_PUBLIC_KEYSTORE_PASSWORD)

            .put(TLS_TRUSTSTORE_PASSWORD, EGlobalParam.MESSAGING_SECURITY_TLS_PUBLIC_KEYSTORE_PASSWORD)
            .put(TLS_KEYSTORE_PASSWORD, EGlobalParam.MESSAGING_SECURITY_TLS_PRIVATE_KEYSTORE_PASSWORD)

            .put(BUSINESS_KEYSTORE_PASSWORD, MESSAGING_SECURITY_BUSINESS_PRIVATE_KEYSTORE_PASSWORD)

            .put(OPERATION_MODE, MESSAGING_OPERATION_MODE)
            .put(USER_COMPRESSION, MESSAGING_COMPRESSION_FLAG)
            .put(MAX_MESSAGE_SIZE, MESSAGING_MESSAGE_SIZE_MAX)
            .put(USE_PULL_MODE, MESSAGING_PULL_MODE_FLAG)
            .put(DEFAULT_PULLING_INTERVAL, MESSAGING_PULL_INTERVAL_DEFAULT)
            .put(MAX_RETRIES, MESSAGING_RETRY_NO_MAX)
            .put(RETRY_INTERVAL, MESSAGING_RETRY_INTERVAL)
            .put(ANTIMALWARE_MODE, MESSAGING_ANTIMALWARE_MODE)
            .put(ANTIMALWARE_SETTINGS, MESSAGING_ANTIMALWARE_SETTINGS)
            .put(ANTIMALWARE_INFECTED_SETTINGS, MESSAGING_ANTIMALWARE_INFECTED)
            .build();

}
