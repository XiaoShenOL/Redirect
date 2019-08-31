package com.lmgy.redirect.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.lmgy.redirect.R
import com.lmgy.redirect.adapter.HostSettingAdapter
import com.lmgy.redirect.base.BaseFragment
import com.lmgy.redirect.db.RepositoryProvider
import com.lmgy.redirect.db.data.HostData
import com.lmgy.redirect.event.MessageEvent
import com.lmgy.redirect.listener.RecyclerItemClickListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * @author lmgy
 * @date 2019/8/29
 */
class RulesFragment : BaseFragment(), Toolbar.OnMenuItemClickListener {

    private var isMultiSelect = false
    private var selectedIds: MutableList<String> = ArrayList()

    private lateinit var mAdapter: HostSettingAdapter
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mRv: RecyclerView
    private lateinit var mTvEmpty: TextView
    private lateinit var mContext: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this.context ?: requireContext()
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rules, container, false)
        initView(view)
        return view
    }


    private fun getList(): MutableList<HostData> {
        val dataList = RepositoryProvider.providerHostRepository(mContext).getAllHosts()
//        val dataList = SPUtils.getDataList(mContext, "hostList", HostData::class.java)
        if (dataList.size == 0) {
            mRv.visibility = View.GONE
            mTvEmpty.visibility = View.VISIBLE
        } else {
            mRv.visibility = View.VISIBLE
            mTvEmpty.visibility = View.GONE
        }
        return dataList
    }

    override fun checkStatus() {
        menu?.findItem(R.id.nav_rules)?.isChecked = true
        toolbar?.inflateMenu(R.menu.menu_list)
        toolbar?.setTitle(R.string.nav_rules)
        toolbar?.setOnMenuItemClickListener(this)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_toggle -> {
                val hostDataList = getList()
                selectedIds.forEach {
                    val hostData = hostDataList[Integer.parseInt(it)]
                    hostData.type = !hostData.type
                }
                RepositoryProvider.providerHostRepository(mContext).updateAll(hostDataList)
//                SPUtils.setDataList(mContext, "hostList", hostDataList)
                mAdapter.setHostDataList(hostDataList)
            }
            R.id.action_add -> {
                NavHostFragment.findNavController(this).navigate(R.id.nav_edit)
            }
        }
        return true
    }

    private fun multiSelect(position: Int) {
        val data = mAdapter.getItem(position)
        if (data != null) {
            if (selectedIds.contains(position.toString())) {
                selectedIds.remove(position.toString())
            } else {
                selectedIds.add(position.toString())
            }
            if (selectedIds.size > 0) {
                toolbar?.title = selectedIds.size.toString()
            } else {
                toolbar?.setTitle(R.string.nav_rules)
            }
            mAdapter.setSelectedIds(selectedIds)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun initData() {
        mAdapter = HostSettingAdapter(mContext, getList())
        mRv.layoutManager = LinearLayoutManager(mContext)
        mRv.adapter = mAdapter

        mRv.addOnItemTouchListener(RecyclerItemClickListener(mContext, mRv, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {

                if (!isMultiSelect) {
                    selectedIds = ArrayList()
                    isMultiSelect = true
                }
                multiSelect(position)
            }

            override fun onItemLongClick(view: View, position: Int) {

                val action = RulesFragmentDirections.actionNavRulesToNavEdit()
                        .setHostData(getList()[position])
                        .setId(position)
                Navigation.findNavController(view).navigate(action)

            }
        }))

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return makeMovementFlags(0, ItemTouchHelper.START or ItemTouchHelper.END)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val dataList = getList()
                val copyDataList = ArrayList<HostData>(dataList)
                dataList.removeAt(position)

                RepositoryProvider.providerHostRepository(mContext).updateAll(dataList)
//                SPUtils.setDataList<HostData>(context, "hostList", dataList)
                mAdapter.setHostDataList(dataList)
                Snackbar.make(view!!, getString(R.string.delete_successful), Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.action_undo)) {
                            RepositoryProvider.providerHostRepository(mContext).updateAll(copyDataList)
//                            SPUtils.setDataList<HostData>(context, "hostList", copyDataList)
                            mAdapter.setHostDataList(copyDataList)
                        }
                        .show()
            }
        })

        itemTouchHelper.attachToRecyclerView(mRv)

        mSwipeRefreshLayout.setOnRefreshListener {
            mAdapter.setHostDataList(getList())
            mSwipeRefreshLayout.isRefreshing = false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBus(event: MessageEvent) {
        if (event.type == 2) {
            mAdapter.setHostDataList(getList())
        }
    }

    private fun initView(view: View) {
        mRv = view.findViewById(R.id.rv)
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mTvEmpty = view.findViewById(R.id.tv_empty)
    }
}
