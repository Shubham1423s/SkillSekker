package com.Shubham.projects.SkillSeeker.Dto;

import java.util.List;

public class AutocompleteResponseDto {
    private List<String> suggestions;

    public AutocompleteResponseDto() {}

    public AutocompleteResponseDto(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
}
