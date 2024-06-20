package vacanciesalert.telegram.update.model;

import lombok.Getter;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class MultiselectTagsCursor {
    public static final int TAGS_PAGE_SIZE = 8;
    private int offset = 0;
    private final LinkedHashMap<Integer, String> allUserTags = new LinkedHashMap<>();
    @Getter
    private LinkedHashMap<Integer, String> tagsOnTheCurrentPage = new LinkedHashMap<>();
    @Getter
    private final Set<String> selectedTags = new HashSet<>();
    private boolean hasNextTags;

    public MultiselectTagsCursor(Set<String> allUserTags) {
        int key = 0;
        for (String tag : allUserTags.stream().sorted().toList()) {
            this.allUserTags.put(key, tag);
            key++;
        }
        changeOffset(0);
    }

    public void toggleTag(String tag) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag);
        } else {
            selectedTags.add(tag);
        }
    }

    public void changeOffset(int delta) {
        if (offset + delta < allUserTags.size()) {
            offset += delta;
            tagsOnTheCurrentPage.clear();
            for (int i = offset; i < offset + TAGS_PAGE_SIZE; i++) {
                if (!allUserTags.containsKey(i)) {
                    break;
                }
                tagsOnTheCurrentPage.put(i, allUserTags.get(i));
            }
        }
        hasNextTags = offset < allUserTags.size() - TAGS_PAGE_SIZE;
    }

    public boolean isSelected(String tag) {
        return selectedTags.contains(tag);
    }

    public int selectedTagCount() {
        return selectedTags.size();
    }

    public boolean hasPrevTags() {
        return offset > 0;
    }

    public boolean hasNextTags() {
        return hasNextTags;
    }

}

