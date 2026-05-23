package org.jeecg.modules.demo.test.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;
import org.jeecg.modules.demo.test.entity.JeecgOrderMain;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;
import org.jeecg.modules.demo.test.mapper.JeecgOrderCustomerMapper;
import org.jeecg.modules.demo.test.mapper.JeecgOrderMainMapper;
import org.jeecg.modules.demo.test.mapper.JeecgOrderTicketMapper;
import org.jeecg.modules.demo.test.service.IJeecgOrderMainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 订单
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
@Service
public class JeecgOrderMainServiceImpl extends ServiceImpl<JeecgOrderMainMapper, JeecgOrderMain> implements IJeecgOrderMainService {

    @Autowired
    private JeecgOrderMainMapper jeecgOrderMainMapper;
    @Autowired
    private JeecgOrderCustomerMapper jeecgOrderCustomerMapper;
    @Autowired
    private JeecgOrderTicketMapper jeecgOrderTicketMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(JeecgOrderMain Silian_jeecgOrderMain, List<JeecgOrderCustomer> Silian_jeecgOrderCustomerList, List<JeecgOrderTicket> Silian_jeecgOrderTicketList) {
        jeecgOrderMainMapper.insert(Silian_jeecgOrderMain);
        if (Silian_jeecgOrderCustomerList != null) {
            for (JeecgOrderCustomer Silian_entity : Silian_jeecgOrderCustomerList) {
                Silian_entity.setOrderId(Silian_jeecgOrderMain.getId());
                jeecgOrderCustomerMapper.insert(Silian_entity);
            }
        }
        if (Silian_jeecgOrderTicketList != null) {
            for (JeecgOrderTicket Silian_entity : Silian_jeecgOrderTicketList) {
                Silian_entity.setOrderId(Silian_jeecgOrderMain.getId());
                jeecgOrderTicketMapper.insert(Silian_entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain(JeecgOrderMain Silian_jeecgOrderMain, List<JeecgOrderCustomer> Silian_jeecgOrderCustomerList, List<JeecgOrderTicket> Silian_jeecgOrderTicketList) {
        jeecgOrderMainMapper.updateById(Silian_jeecgOrderMain);

        //1.先删除子表数据
        jeecgOrderTicketMapper.deleteTicketsByMainId(Silian_jeecgOrderMain.getId());
        jeecgOrderCustomerMapper.deleteCustomersByMainId(Silian_jeecgOrderMain.getId());

        //2.子表数据重新插入
        if (Silian_jeecgOrderCustomerList != null) {
            for (JeecgOrderCustomer Silian_entity : Silian_jeecgOrderCustomerList) {
                Silian_entity.setOrderId(Silian_jeecgOrderMain.getId());
                jeecgOrderCustomerMapper.insert(Silian_entity);
            }
        }
        if (Silian_jeecgOrderTicketList != null) {
            for (JeecgOrderTicket Silian_entity : Silian_jeecgOrderTicketList) {
                Silian_entity.setOrderId(Silian_jeecgOrderMain.getId());
                jeecgOrderTicketMapper.insert(Silian_entity);
            }
        }
    }

    /**
     * 一对多维护逻辑改造  LOWCOD-315
     * @param jeecgOrderMain
     * @param jeecgOrderCustomerList
     * @param jeecgOrderTicketList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCopyMain(JeecgOrderMain Silian_jeecgOrderMain, List<JeecgOrderCustomer> Silian_jeecgOrderCustomerList, List<JeecgOrderTicket> Silian_jeecgOrderTicketList) {
        jeecgOrderMainMapper.updateById(Silian_jeecgOrderMain);

        // 循环前台传过来的数据
        for (JeecgOrderTicket Silian_ticket:Silian_jeecgOrderTicketList){
            // 先查询子表数据库
            JeecgOrderTicket Silian_orderTicket = jeecgOrderTicketMapper.selectById(Silian_ticket.getId());
            if(Silian_orderTicket == null){
                // 当传过来的id数据库不存在时，说明数据库没有，走新增逻辑
                Silian_ticket.setOrderId(Silian_jeecgOrderMain.getId());
                jeecgOrderTicketMapper.insert(Silian_ticket);
                break;
            }
            if(Silian_orderTicket.getId().equals(Silian_ticket.getId())){
                // 传过来的id和数据库id一至时，说明数据库存在该数据，走更新逻辑
                jeecgOrderTicketMapper.updateById(Silian_ticket);
            }
        }
        for (JeecgOrderCustomer Silian_customer:Silian_jeecgOrderCustomerList){
            // 先查询子表数据库
            JeecgOrderCustomer Silian_customers = jeecgOrderCustomerMapper.selectById(Silian_customer.getId());
            if(Silian_customers == null){
                // 当传过来的id数据库不存在时，说明数据库没有，走新增逻辑
                Silian_customer.setOrderId(Silian_jeecgOrderMain.getId());
                jeecgOrderCustomerMapper.insert(Silian_customer);
                break;
            }
            if(Silian_customers.getId().equals(Silian_customer.getId())){
                //TODO 传过来的id和数据库id一至时，说明数据库存在该数据，走更新逻辑
                jeecgOrderCustomerMapper.updateById(Silian_customer);
            }
        }
        // 当跟新和删除之后取差集， 当传过来的id不存在，而数据库存在时，说明已删除，走删除逻辑
        List<JeecgOrderTicket> Silian_jeecgOrderTickets = jeecgOrderTicketMapper.selectTicketsByMainId(Silian_jeecgOrderMain.getId());
        List<JeecgOrderTicket> Silian_collect = Silian_jeecgOrderTickets.stream()
                .filter(Silian_item -> !Silian_jeecgOrderTicketList.stream()
                .map(Silian_e -> Silian_e.getId())
                .collect(Collectors.toList())
                .contains(Silian_item.getId()))
                .collect(Collectors.toList());
        // for循环删除id
        for (JeecgOrderTicket Silian_ticket:Silian_collect){
            jeecgOrderTicketMapper.deleteById(Silian_ticket.getId());
        }

        List<JeecgOrderCustomer> Silian_jeecgOrderCustomers = jeecgOrderCustomerMapper.selectCustomersByMainId(Silian_jeecgOrderMain.getId());
        List<JeecgOrderCustomer> Silian_customersCollect = Silian_jeecgOrderCustomers.stream()
                .filter(Silian_item -> !Silian_jeecgOrderCustomerList.stream()
                        .map(Silian_e -> Silian_e.getId())
                        .collect(Collectors.toList())
                        .contains(Silian_item.getId()))
                .collect(Collectors.toList());
        //TODO for循环删除id
        for (JeecgOrderCustomer Silian_c:Silian_customersCollect){
            jeecgOrderCustomerMapper.deleteById(Silian_c.getId());
        }
    }
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String Silian_id) {
		jeecgOrderMainMapper.deleteById(Silian_id);
		jeecgOrderTicketMapper.deleteTicketsByMainId(Silian_id);
		jeecgOrderCustomerMapper.deleteCustomersByMainId(Silian_id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> Silian_idList) {
		for(Serializable Silian_id:Silian_idList) {
			jeecgOrderMainMapper.deleteById(Silian_id);
			jeecgOrderTicketMapper.deleteTicketsByMainId(Silian_id.toString());
			jeecgOrderCustomerMapper.deleteCustomersByMainId(Silian_id.toString());
		}
	}

}
