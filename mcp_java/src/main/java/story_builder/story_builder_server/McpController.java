package story_builder.story_builder_server;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
public class McpController {

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSse() {
        SseEmitter emitter = new SseEmitter();
        try {
            emitter.send(SseEmitter.event().name("init").data("SSE stream started"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }
}
