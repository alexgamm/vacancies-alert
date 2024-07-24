package vacanciesalert.telegram.update.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class MultiselectTagsCursor {
    public static final int TAGS_PAGE_SIZE = 8;
    private int offset = 0;
    private final List<String> allUserTags = new ArrayList<>();
    @Getter
    private final List<String> tagsOnTheCurrentPage =  new ArrayList<>();
    @Getter
    private final Set<String> selectedTags = new HashSet<>();
    private boolean hasNextTags;

    public MultiselectTagsCursor(List<String> allUserTags) {
        this.allUserTags.addAll(allUserTags);
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
                if (allUserTags.size() <= i) {
                    break;
                }
                tagsOnTheCurrentPage.add(allUserTags.get(i));
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

