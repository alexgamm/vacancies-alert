package vacanciesalert.utils;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TagsParserTest {
    @Test
    public void parseTags() {
        //arrange
        String source = "кошка с мышкой, гуси лебеди, саша";
        Set<String> supposedOutput = Set.of("кошка с мышкой", "гуси лебеди", "саша");
        //act
        Set<String> output = TagsParser.parse(source);
        //assert
        assertEquals(supposedOutput, output);
    }
}
