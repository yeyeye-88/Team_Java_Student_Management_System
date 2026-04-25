package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.DictionaryInfo;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.DictionaryInfoRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryInfoService {
    private final DictionaryInfoRepository dictionaryInfoRepository;

    public DictionaryInfoService(DictionaryInfoRepository dictionaryInfoRepository) {
        this.dictionaryInfoRepository = dictionaryInfoRepository;
    }

    public DataResponse findAll() {
        List<DictionaryInfo> dictionaryList = dictionaryInfoRepository.findAll();
        return CommonMethod.getReturnData(dictionaryList);
    }

    public DataResponse findRootList() {
        try {
            List<DictionaryInfo> dictionaryList = dictionaryInfoRepository.findRootList();
            return CommonMethod.getReturnData(dictionaryList);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("查询根字典失败：" + e.getMessage());
        }
    }
}
