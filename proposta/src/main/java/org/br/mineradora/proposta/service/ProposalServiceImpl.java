package org.br.mineradora.proposta.service;

import org.br.mineradora.proposta.dto.ProposalDTO;
import org.br.mineradora.proposta.dto.ProposalDetailsDTO;
import org.br.mineradora.proposta.entity.ProposalEntity;
import org.br.mineradora.proposta.exception.NotFoundException;
import org.br.mineradora.proposta.kafka.KafkaEvents;
import org.br.mineradora.proposta.repository.ProposalRepository;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProposalServiceImpl implements ProposalService {

    private final ProposalRepository proposalRepository;

    private final KafkaEvents kafkaEvents;

    @Override
    public ProposalDetailsDTO findByIdDTO(long id) {
        ProposalEntity proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proposal not found!"));
        return new ProposalDetailsDTO(proposal);
    }

    public void createNewProposal(ProposalDetailsDTO proposalDetailsDTO) {
        ProposalDTO proposalDTO = buildAndSaveNewProposal(proposalDetailsDTO);
        kafkaEvents.sendNewKafkaEvent(proposalDTO);
    }

    private ProposalDTO buildAndSaveNewProposal(ProposalDetailsDTO proposalDetailsDTO) {
        ProposalEntity proposal = proposalDetailsDTO.toEntity(proposalDetailsDTO);

        proposalRepository.save(proposal);

        return ProposalDTO.builder()
                .proposalId(proposal.getId())
                .priceTonne(proposal.getPriceTonne())
                .customer(proposal.getCustomer())
                .build();
    }

    @Override
    public void removeProposal(long id) {
        proposalRepository.deleteById(id);
    }

}
