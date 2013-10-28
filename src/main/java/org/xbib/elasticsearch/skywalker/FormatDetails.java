
package org.xbib.elasticsearch.skywalker;

/**
 *  Format details
 */
public class FormatDetails {

    private String genericName = "N/A";

    private String capabilities = "N/A";

    private String version = "N/A";

    public FormatDetails(String capabilities, String genericName, String version) {
        this.genericName = genericName;
        this.capabilities = capabilities;
        this.version = version;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

}
