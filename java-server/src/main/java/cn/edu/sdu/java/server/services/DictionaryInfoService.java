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

    /**
     * 根据字典类型 code 查询字典列表
     */
    public DataResponse findByCode(String code) {
        try {
            List<DictionaryInfo> dictionaryList = dictionaryInfoRepository.getDictionaryInfoList(code);
            return CommonMethod.getReturnData(dictionaryList);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("查询字典失败：" + e.getMessage());
        }
    }

    /**
     * 根据父节点 ID 查询子字典
     */
    public DataResponse findByPid(Integer pid) {
        try {
            List<DictionaryInfo> dictionaryList = dictionaryInfoRepository.findByPid(pid);
            return CommonMethod.getReturnData(dictionaryList);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("查询子字典失败：" + e.getMessage());
        }
    }

    /**
     * 查询根字典列表
     */
    public DataResponse findRootList() {
        try {
            List<DictionaryInfo> dictionaryList = dictionaryInfoRepository.findRootList();
            return CommonMethod.getReturnData(dictionaryList);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("查询根字典失败：" + e.getMessage());
        }
    }
}
