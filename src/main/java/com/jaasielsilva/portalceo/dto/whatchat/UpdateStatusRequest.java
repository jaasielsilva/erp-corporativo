package com.jaasielsilva.portalceo.dto.whatchat;

import com.jaasielsilva.portalceo.model.whatchat.ChatConversaStatus;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    private ChatConversaStatus status;
}

