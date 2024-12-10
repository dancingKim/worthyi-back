package com.worthyi.worthyi_backend.service;

import com.worthyi.worthyi_backend.model.dto.ActionDto;
import com.worthyi.worthyi_backend.model.dto.AdultActionDto;
import com.worthyi.worthyi_backend.model.entity.*;
import com.worthyi.worthyi_backend.repository.*;
import com.worthyi.worthyi_backend.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ActionService {
    private final PlaceInstanceRepository placeInstanceRepository;
    private final AvatarRepository avatarRepository;

    private final ChildActionTemplateRepository childActionTemplateRepository;
    private final AdultActionTemplateRepository adultActionTemplateRepository;
    private final ChildActionInstanceRepository childActionInstanceRepository;
    private final AdultActionInstanceRepository adultActionInstanceRepository;
    private final UserRepository userRepository;

    public ActionDto.Response saveChildAction(ActionDto.Request actionDto, PrincipalDetails user) {
        log.info("actionService Starts");


        PlaceInstance placeInstance = placeInstanceRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("PlaceInstance not found"));
        ChildActionTemplate childActionTemplate = childActionTemplateRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("ActionTemplate not found"));
        AdultActionTemplate adultActionTemplate = adultActionTemplateRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("ActionTemplate not found"));
        Avatar avatar = avatarRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Avatar not found"));

        // toEntity()에서 기본적인 ActionInstance 빌드 후, service에서 관계 설정
        ChildActionInstance childActionInstance = actionDto.toEntity(actionDto);
        childActionInstance.setPlaceInstance(placeInstance);
        childActionInstance.setChildActionTemplate(childActionTemplate);
        childActionInstance.setAvatarId(avatar.getAvatarId());
        childActionInstance.setChildActionTemplate(childActionTemplate);
        ;
        // 실제로는 principal에 해당하는 user, avatar를 mapping 해야 하지만, 여기선 avatar_id=1
        // 만약 ActionInstance에 avatar 직접 연결이 필요하다면 AvatarInVillage나 ActionResult에서 처리
        // ActionInstance 자체에는 avatar_id가 없다면, 나중에 ActionResult 생성 시 avatar를 연결

        log.info("Saving action {}", childActionInstance);
        // DB 저장
        ChildActionInstance instance = childActionInstanceRepository.save(childActionInstance);

        return ActionDto.Response.fromEntity(instance);
    }

    public AdultActionDto.Response saveAdultAction(AdultActionDto.Request actionDto, String email) {
        AdultActionTemplate adultActionTemplate = adultActionTemplateRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("ActionTemplate not found"));
        ChildActionInstance childActionInstance = childActionInstanceRepository.findById(actionDto.getChildActionId())
                .orElseThrow(() -> new RuntimeException("ActionTemplate not found"));

        AdultActionInstance adultActionInstance = actionDto.toEntity(actionDto);

        adultActionInstance.setChildActionInstance(childActionInstance);
        adultActionInstance.setAdultActionTemplate(adultActionTemplate);
        adultActionInstance.setUserId(1L);

        AdultActionInstance instance = adultActionInstanceRepository.save(adultActionInstance);


        return AdultActionDto.Response.fromEntity(instance);
    }
}
