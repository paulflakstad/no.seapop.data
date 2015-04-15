package no.seapop.data;

import java.util.Arrays;
import java.util.List;
import no.npolar.util.CmsAgent;

/**
 * Population | Reproduction | Survival | Diet
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute <flakstad at npolar.no>
 */
public class SpeciesDataLinkType {
    
    /** Type name: Population */
    public static final String POPULATION = "Population";
    /** Type name: Reproduction */
    public static final String REPRODUCTION = "Reproduction";
    /** Type name: Survival */
    public static final String SURVIVAL = "Survival";
    /** Type name: Diet */
    public static final String DIET = "Diet";
    /** Type name: Timing (phenology) */
    public static final String TIMING = "Timing";
    
    /** The default ordering of the types. */
    public static final List<String> TYPES_ORDER_DEFAULT = Arrays.asList(new String[] { POPULATION, REPRODUCTION, SURVIVAL, DIET, TIMING });
    
    /** The type name. */
    private String name = null;
    /** The order factor. */
    private int orderFactor = -1;
    /** The identifier string, typically the type name in all-lowercase letters. */
    private String identifier = null;
    
    /**
     * Creates a new data link type, based on the given type name.
     * 
     * @param typeName The type name, should be one of the names defined as 
     * static finals in this class.
     */
    public SpeciesDataLinkType(String typeName) {
        name = typeName;
        identifier = name.toLowerCase();
        orderFactor = TYPES_ORDER_DEFAULT.indexOf(name);
    }
    
    /**
     * Gets the order factor, a number indicating the position of this type 
     * when it appears in a set of types.
     * <p>
     * The constructor sets the initial order factor. It does so by looking up 
     * the index of this type's name within the {@link #TYPES_ORDER_DEFAULT} 
     * list. (This also means that if the type name is not in this list, the 
     * order factor will be -1.)
     * 
     * @return The order factor.
     */
    public int getOrderFactor() { return orderFactor; }
    
    /**
     * Gets the (type) name, for example "Population".
     * <p>
     * The name should be one of the names defined as static finals in this 
     * class.
     * 
     * @return The (type) name, for example "Population".
     */
    public String getName() { return name; }
    
    /**
     * Gets the identifier, typically the type name in all-lowercase letters.
     * 
     * @return The identifier.
     */
    public String getIdentifier() { return identifier; }
    
    /**
     * Gets the identifier for the given type name. 
     * 
     * @param name The type name.
     * @return The identifier for the given type name.
     */
    public static String getIdentifierForName(String name) { return name.toLowerCase(); }
    
    
    /**
     * Gets the corresponding localized label.
     * 
     * @param cms An initialized action element, holding the locale.
     * @return The corresponding label, localized according to the given CmsAgent instance.
     */
    public String getLabel(CmsAgent cms) {
        return cms.labelUnicode("label.seapop-species-data.category.".concat(name.toLowerCase()));
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SpeciesDataLinkType))
            return false;
        if (obj == this)
            return true;

        SpeciesDataLinkType rhs = (SpeciesDataLinkType) obj;
        return this.name.equals(rhs.name);
    }
}