package no.seapop.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import no.npolar.util.CmsAgent;
import org.opencms.jsp.I_CmsXmlContentContainer;

/**
 * Container class: Holds all data about a single species, including a list of 
 * links to the raw data (typically time series).
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute <flakstad at npolar.no>
 */
public class SpeciesData {
    /**
     * The species name.
     */
    private String name = null;
    /**
     * The URI to the species file in the OpenCms virtual file system.
     */
    private String uri = null;    
    //private String speciesFileUri = null;
    private boolean isPelagic = false;
    private boolean isSurfaceBound = false;
    private boolean isDiving = false;
    private boolean isCoastalBound = false;
    private boolean isIceBound = false;
    /**
     * The data links.
     */
    private List<SpeciesDataLink> dataLinks = null;
    
    public static final String RESOURCE_TYPE_NAME = "seapop_species_data";
    
    /**
     * Compares by group number first, and second (for identical group numbers) by name.
     */
    public static final Comparator<SpeciesData> GROUP_NUMBER = new Comparator<SpeciesData>() {
        @Override
        public int compare(SpeciesData o1, SpeciesData o2) {
            int diff = Integer.valueOf(o1.getGroupNumber()).compareTo(Integer.valueOf(o2.getGroupNumber()));
            if (diff == 0) 
                return o1.getName().compareTo(o2.getName()); // Same group, compare by name
            return diff;
        }
    };
    
    /**
     * Creates a new instance by reading the given species file URI.
     * 
     * @param speciesFileUri The species file URI.
     * @param cms An initialized CmsAgent.
     */
    public SpeciesData(String speciesFileUri, CmsAgent cms) {
        this.uri = speciesFileUri;
        this.dataLinks = new ArrayList<SpeciesDataLink>();
        try {
            I_CmsXmlContentContainer container = cms.contentload("singleFile", uri, cms.getRequestContext().getLocale(), false);
            while (container.hasMoreResources()) {
                this.name = cms.contentshow(container, "SpeciesName");
                this.isPelagic = Boolean.valueOf(cms.contentshow(container, "Pelagic")).booleanValue();
                this.isCoastalBound = Boolean.valueOf(cms.contentshow(container, "CoastalBound")).booleanValue();
                this.isSurfaceBound = Boolean.valueOf(cms.contentshow(container, "SurfaceBound")).booleanValue();
                this.isDiving = Boolean.valueOf(cms.contentshow(container, "Diving")).booleanValue();
                this.isIceBound = Boolean.valueOf(cms.contentshow(container, "IceBound")).booleanValue();
                
                try {
                    I_CmsXmlContentContainer locationContainer = cms.contentloop(container, "DataLinks");
                    while (locationContainer.hasMoreResources()) {
                        String location = cms.contentshow(locationContainer, "Location");
                        I_CmsXmlContentContainer dataLinksContainer = cms.contentloop(locationContainer, "DataLink");
                        while (dataLinksContainer.hasMoreResources()) {
                            String dataLinkType = cms.contentshow(dataLinksContainer, "Type");
                            String dataLinkURL = cms.contentshow(dataLinksContainer, "URL");
                            String dataLinkNumYears = cms.contentshow(dataLinksContainer, "NumOfYears");
                            String dataLinkComment = cms.contentshow(dataLinksContainer, "Comment");
                            this.addDataLink(location, dataLinkType, dataLinkURL, dataLinkNumYears, CmsAgent.elementExists(dataLinkComment) ? dataLinkComment : null);
                        }
                    }
                } catch (Exception ee) {
                    
                }
            }
        } catch (Exception e) {
            
        }
    }
    public boolean isPelagic() { return isPelagic; }
    public boolean isCoastalBound() { return isCoastalBound; }
    public boolean isSurfaceBound() { return isSurfaceBound; }
    public boolean isDiving() { return isDiving; }
    public boolean isIceBound() { return isIceBound; }
    
    /**
     * Gets the URI to the species file in the OpenCms virtual file system.
     * 
     * @return The URI to the species file in the OpenCms virtual file system.
     */
    public String getVfsUri() { return uri; }
    
    //public String getSpeciesFileVfsUri() { return speciesFileUri; }
    
    /**
     * <p>
     * Gets the group number.
     * </p>
     * <ul>
     * <li>0: Pelagic AND diving</li>
     * <li>1: Pelagic AND diving / surface bound</li>
     * <li>2: Pelagic AND surface bound</li>
     * <li>3: Pelagic / ice bound</li>
     * <li>4: Pelagic / coastal bound<li>
     * <li>5: Coastal bound AND surface bound</li>
     * <li>6: Coastal bound AND diving / surface bound</li>
     * <li>7: Coastal bound AND diving</li>
     * 
     * @return The group number.
     */
    public int getGroupNumber() {
        int i = -1;
        if (isPelagic()) {
            if (isDiving()) { 
                i = 0; 
                if (isSurfaceBound()) {
                    i = 1;
                }
            } else if (isSurfaceBound()) { 
                i = 2;
            } 
            
            if (isIceBound()) {
                i = 3;
            }
            if (isCoastalBound()) {
                i = 4;
            }
        } else {
            if (isSurfaceBound()) {
                i = 5; 
                if (isDiving()) {
                    i = 6;
                }
            } else if (isDiving()) {
                i = 7;
            }
        }
        
        return i;
    }
    /**
     * Adds a single data link to the list of data links.
     * 
     * 
     * @param location The location. Should always be given.
     * @param type The type/category. Should always be given.
     * @param url The URL to the data. Should always be given.
     * @param numYears The number of years data exists for (optional).
     * @param comment The comment, if any (optional).
     * @return This instance, updated.
     */
    public SpeciesData addDataLink(String location, String type, String url, String numYears, String comment) {
        this.dataLinks.add(new SpeciesDataLink(location, type, url, numYears, comment));
        return this;
    }
    /**
     * Gets all data links for a given location.
     * 
     * @param location The location.
     * @return All data links for the given location, or an empty list.
     */
    public List<SpeciesDataLink> getDataLinksByLocation(String location) {
        List<SpeciesDataLink> tmp = new ArrayList<SpeciesDataLink>();
        Iterator<SpeciesDataLink> i = this.dataLinks.iterator();
        while (i.hasNext()) {
            SpeciesDataLink sdl = i.next();
            try {
                if (sdl.getLocation().equals(location)) {
                    tmp.add(sdl);
                }
            } catch (NullPointerException npe) {
                // missing location => ignore, and continue (or not)
            }
        }
        return tmp;
    }
    /**
     * Gets the list of locations for all the data links.
     * @return The list of locations for all the data links, or an empty list.
     */
    public List<String> getLocations() {
        List<String> tmp = new ArrayList<String>();
        Iterator<SpeciesDataLink> i = this.dataLinks.iterator();
        while (i.hasNext()) {
            SpeciesDataLink sdl = i.next();
            String location = sdl.getLocation();
            if (location != null && !tmp.contains(location)) {
                tmp.add(location);
            }
        }
        return tmp;
    }
    /**
     * Gets the data links.
     * @return The data links, or an empty list.
     */
    public List<SpeciesDataLink> getDataLinks() { return this.dataLinks; }
    /**
     * Gets the species name.
     * @return The species name.
     */
    public String getName() { return this.name; }
}
