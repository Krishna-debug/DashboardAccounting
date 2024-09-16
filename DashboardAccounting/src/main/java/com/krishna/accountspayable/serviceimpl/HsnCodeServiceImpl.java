package com.krishna.accountspayable.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.accountspayable.domain.HsnCode;
import com.krishna.accountspayable.repository.HsnCodeRepository;
import com.krishna.accountspayable.services.HsnCodeService;

@Service
public class HsnCodeServiceImpl implements HsnCodeService {

	@Autowired private HsnCodeRepository hsnCodeRepository;
	
	@Override
	public HsnCode saveHSNCode(String hsnCode) {
		boolean isExist = IsExistingEntry(hsnCode);
		if(isExist) {
			return hsnCodeRepository.findByHsnCodeAndIsArchive(hsnCode, false);
		} else {
			HsnCode code = HsnCode.builder().hsnCode(hsnCode).build();
			return hsnCodeRepository.save(code);
		}
	}

	private boolean IsExistingEntry(String hsnCode) {
		HsnCode code = hsnCodeRepository.findByHsnCodeAndIsArchive(hsnCode, false);
		if(code != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<HsnCode> getAllHsnCodes() {
		List<HsnCode> codes = hsnCodeRepository.findAllByIsArchive(false);
		return codes;
	}

	@Override
	public HsnCode updateHsnCode(long id, String updatedHsnCode) {
		HsnCode code = hsnCodeRepository.findById(id);
		if(code != null) {
			code.setHsnCode(updatedHsnCode);
			return hsnCodeRepository.saveAndFlush(code);
		}
		return null;
	}

	@Override
	public HsnCode deleteHsnData(long id) {
		HsnCode code = hsnCodeRepository.findById(id);
		if(code != null) {
			code.setArchive(true);
			return hsnCodeRepository.saveAndFlush(code);
		}
		return null;
	}
	
	
}
