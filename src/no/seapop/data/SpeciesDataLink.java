package no.seapop.data;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import no.npolar.util.CmsAgent;

/**
 * Represents a link to a data set. 
 * <p>
 * The data set is related to a specific geographical location, and is a time 
 * series spanning X years. It is also of a specific type category (i.e. 
 * population, survival etc.).
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute <flakstad at npolar.no>
 */
public class SpeciesDataLink {
    private String url = null;
    private String location = null;
    private SpeciesDataLinkType type = null; // Population | Reproduction | Survival | Diet
    private String numYears = null;
    private String comment = null;
    
    /**
     * Dummy constructor
     */
    public SpeciesDataLink() {
        this(null, null, null, null, null);
    }
    /**
     * Creates a new data link.
     * 
     * @param location The associated geographical location.
     * @param type The data type category.
     * @param url The target URL.
     * @param numYears How many years data exists for.
     * @param comment Optional comment.
     * @see SpeciesDataLinkType
     */
    public SpeciesDataLink(String location, String type, String url, String numYears, String comment) {
        this.location = location;
        this.url = url;
        this.type = new SpeciesDataLinkType(type);
        this.numYears = numYears;
        this.comment = comment;
    }
    
    /**
     * Gets the associated geographical location.
     * 
     * @return The associated geographical location.
     */
    public String getLocation() { return this.location; }
    /**
     * Gets the target URL.
     * 
     * @return The target URL.
     */
    public String getUrl() { return this.url; }
    /**
     * Gets the data link type.
     * 
     * @return The data link type.
     */
    public SpeciesDataLinkType getType() { return this.type; }
    //public String getTypeIdentifier() { return this.type.getIdentifier(); }
    
    //public String getTypeLabel(CmsAgent cms) {
    //    return cms.labelUnicode("label.seapop-species-data.category.".concat(type.toLowerCase()));
    //}
    /**
     * Gets the number of years data exists for.
     * 
     * @return The number of years data exists for.
     */
    public String getNumYears() { return this.numYears; }
    /** 
     * Gets the comment, if any.
     * 
     * @return The comment, if any.
     */
    public String getComment() { return this.comment; }
    //public static List<String> getTypes() { return Arrays.asList( new String[] { "Diet", "Population", "Reproduction", "Survival" } ); }
}
