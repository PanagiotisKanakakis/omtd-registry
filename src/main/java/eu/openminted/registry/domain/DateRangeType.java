package eu.openminted.registry.domain;

/**
 * Created by stefania on 9/5/16.
 */
public class DateRangeType {

    //required
    private DateType startDate;
    //required
    private DateType endDate;

    public DateRangeType() {
    }

    public DateRangeType(DateType startDate, DateType endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public DateType getStartDate() {
        return startDate;
    }

    public void setStartDate(DateType startDate) {
        this.startDate = startDate;
    }

    public DateType getEndDate() {
        return endDate;
    }

    public void setEndDate(DateType endDate) {
        this.endDate = endDate;
    }
}