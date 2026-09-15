package io.github.mortuusars.exposure.util;

import io.github.mortuusars.exposure.util.supporter.Supporter;
import io.github.mortuusars.exposure.util.supporter.Supporters;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SupportersTests {
    @Test
    void parsingCorrectlyFromJson() {
        String json =
        """
        [
            {
             "name": "player1",
             "uuid": "19266046-b14b-428f-919b-75a21474ba07"
            },
            {
             "name": "player2",
             "uuid": "11111111-b14b-428f-919b-75a21474ba07"
            }
        ]
        """;

        List<Supporter> supporters = new Supporters.Loader().parseSupporters(json);

        assertEquals(2, supporters.size());
        assertEquals("player1", supporters.getFirst().name());
        assertEquals(UUID.fromString("19266046-b14b-428f-919b-75a21474ba07"), supporters.getFirst().uuid());
        assertEquals("player2", supporters.get(1).name());
        assertEquals(UUID.fromString("11111111-b14b-428f-919b-75a21474ba07"), supporters.get(1).uuid());
    }
}
