package com.iftm.client.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.repositories.ClientRepository;
import com.iftm.client.services.exceptions.DatabaseException;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import com.iftm.client.services.util.Validador;

@Service
public class ClientService {
	
	@Autowired
	private ClientRepository repository;	
	
	@Autowired
	private Validador validador;
	
	@Transactional(readOnly = true)
	public Page<ClientDTO> findAllPaged(PageRequest pageRequest) {
		Page<Client> list =  repository.findAll(pageRequest);
		return list.map(x -> new ClientDTO(x));
	}
	
	@Transactional(readOnly = true)
	public ClientDTO findById(Long id) {
		Optional<Client> obj = repository.findById(id);
		Client entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new ClientDTO(entity);
	}
	
	@Transactional(readOnly = true)
	public Page<ClientDTO> findByIncome(PageRequest pageRequest, Double income) {
		Page<Client> list = repository.findByIncome(income, pageRequest);		
		return list.map(x -> new ClientDTO(x));
	}

	@Transactional(readOnly = true)
	public Page<ClientDTO> findByIncomeGreaterThan(PageRequest pageRequest, double income) {
		Page<Client> list = repository.findByIncomeGreaterThan(income, pageRequest);
		return list.map(x -> new ClientDTO(x));
	}
	
	@Transactional(readOnly = true)
	public Page<ClientDTO> findByCpfLike(PageRequest pageRequest, String cpf) {
		Page<Client> list = repository.findByCpfLike(cpf, pageRequest);
		return list.map(x -> new ClientDTO(x));
	}

	@Transactional(readOnly = true)
	public Page<ClientDTO> findClientByChildrenGreaterThanEqualOrderByNameAsc(Integer numeroFilhos, PageRequest pageRequest) {
		// Chama o método do repositório que retorna uma List<Client>
		List<Client> lista = repository.findClientByChildrenGreaterThanEqualOrderByNameAsc(numeroFilhos);

		// Calcula os índices de início e fim para a sublista da página atual
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), lista.size());

        List<Client> pageContent = new ArrayList<>();
        if (start < lista.size()) {
            pageContent = lista.subList(start, end);
        }

        // Mapeia os Clientes da sublista para ClientDTOs
        List<ClientDTO> dtoList = pageContent.stream()
                                             .map(ClientDTO::new)
                                             .collect(Collectors.toList());

        // Retorna uma PageImpl, que é uma implementação de Page, contendo a sublista
        // e o total de elementos da lista original.
        return new PageImpl<>(dtoList, pageRequest, lista.size());
	}
	
	@Transactional
	public ClientDTO insert(ClientDTO dto) {		
		Client entity = dto.toEntity();
		entity = repository.save(entity);
		return new ClientDTO(entity);
	}
	
	@Transactional
	public ClientDTO update(Long id, ClientDTO dto) {
		try {
			Client entity = repository.getOne(id);
			updateData(entity, dto);
			entity = repository.save(entity);
			return new ClientDTO(entity);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
	}
	
	public void delete(Long id) {
		validador.eValido(id);
		try {		
			
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		} 
	}

	private void updateData(Client entity, ClientDTO dto) {
		entity.setName(dto.getName());
		entity.setCpf(dto.getCpf());
		entity.setIncome(dto.getIncome());
		entity.setBirthDate(dto.getBirthDate());
		entity.setChildren(dto.getChildren());
	}
}