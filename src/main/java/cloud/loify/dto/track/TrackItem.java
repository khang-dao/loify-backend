package cloud.loify.dto.track;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.DEDUCTION;
import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(use = DEDUCTION)
@JsonSubTypes({@Type(value = TrackItemDTO.class), @Type(value = TrackItemObjectDTO.class)})
public sealed interface TrackItem permits TrackItemDTO, TrackItemObjectDTO {}
