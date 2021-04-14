package eu.ec.dgempl.eessi.rina.tool.migration.common.model;

import com.opencsv.bean.CsvBindByPosition;

public class OrganisationCsv {

    @CsvBindByPosition(position = 0)
    public String id;
    @CsvBindByPosition(position = 1)
    public String country;
    @CsvBindByPosition(position = 2)
    public String CLDId;
    @CsvBindByPosition(position = 3)
    public String activationDate;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getCLDId() {
        return CLDId;
    }

    public void setCLDId(final String CLDId) {
        this.CLDId = CLDId;
    }

    public String getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(final String activationDate) {
        this.activationDate = activationDate;
    }

    @Override
    public String toString() {
        return "OrganisationCsvDto{" + "id='" + id + '\'' + ", country='" + country + '\'' + ", CLDId='" + CLDId + '\''
                + ", activationDate='" + activationDate + '\'' + '}';
    }
}
