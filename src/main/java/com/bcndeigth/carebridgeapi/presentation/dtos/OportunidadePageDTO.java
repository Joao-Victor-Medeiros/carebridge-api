package com.bcndeigth.carebridgeapi.presentation.dtos;

import java.util.List;

public class OportunidadePageDTO {
    public List<OportunidadeItemDTO> items;
    public int page;
    public int totalPages;
    public int totalItems;

    public static class OportunidadeItemDTO {
        public String id;
        public String title;
        public String specialty;
        public String modality;
        public String offerType;
        public int vacancies;
        public ProviderDTO provider;
        public LocationDTO location;
        public String summary;
        public List<String> requiredDocuments;
        public String updatedAt;
    }

    public static class ProviderDTO {
        public String id;
        public String name;
        public String kind;
        public boolean verified;
    }

    public static class LocationDTO {
        public String city;
        public String state;
        public String address;
        public Double distanceKm;
    }
}