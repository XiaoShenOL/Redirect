package com.lmgy.redirect.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
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
import com.lmgy.redirect.db.data.HostData
import com.lmgy.redirect.event.MessageEvent
import com.lmgy.redirect.listener.RecyclerItemClickListener
import com.lmgy.redirect.viewmodel.HostViewModel
import com.lmgy.redirect.viewmodel.HostViewModelFactory
import com.lmgy.redirect.viewmodel.Injection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * @author lmgy
 * @date 2019/8/29
 */
class RulesFragment : BaseFragment(), Toolbar.OnMenuItemClickListener {


    private lateinit var mAdapter: HostSettingAdapter
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mRv: RecyclerView
    private lateinit var mTvEmpty: TextView
    private lateinit var mContext: Context
    private lateinit var hostViewModelFactory: HostViewModelFactory
    private lateinit var viewModel: HostViewModel
    private lateinit var mView: View

    private var hostList: MutableList<HostData> = mutableListOf()
    private val disposable = CompositeDisposable()
    private var isMultiSelect = false
    private var selectedIds: MutableList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this.context ?: requireContext()
        hostViewModelFactory = Injection.provideHostViewModelFactory(mContext)
        viewModel = ViewModelProvider(this, hostViewModelFactory).get(HostViewModel::class.java)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_rules, container, false)
        initView(mView)
        return mView
    }


    override fun onStop() {
        super.onStop()
        disposable.clear()
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
                selectedIds.forEach {
                    val hostData = hostList[Integer.parseInt(it)]
                    hostData.type = !hostData.type
                }
                mAdapter.setHostDataList(hostList)
                selectedIds.forEach {
                    mAdapter.notifyItemChanged(Integer.parseInt(it))
                }
                disposable.add(viewModel.updateAll(hostList)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe())
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


    private fun showData() {
        mRv.layoutManager = LinearLayoutManager(mContext)
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
                        .setHostData(hostList[position])
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
                val copyDataList = ArrayList<HostData>(hostList)
                hostList.removeAt(position)
                mAdapter.setHostDataList(hostList)
                disposable.add(viewModel.updateAll(hostList)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe())
                Snackbar.make(mView, getString(R.string.delete_successful), Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.action_undo)) {
                            mAdapter.setHostDataList(copyDataList)
                            hostList = copyDataList
                            disposable.add(viewModel.updateAll(copyDataList)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe())
                        }
                        .show()
            }
        })
        itemTouchHelper.attachToRecyclerView(mRv)

        mSwipeRefreshLayout.setOnRefreshListener {
            initData()
            mSwipeRefreshLayout.isRefreshing = false
        }
    }

    private fun checkDns() {
        if (hostList.size == 0) {
            mRv.visibility = View.GONE
            mTvEmpty.visibility = View.VISIBLE
        } else {
            mRv.visibility = View.VISIBLE
            mTvEmpty.visibility = View.GONE
        }
        if (mRv.adapter == null) {
            mAdapter = HostSettingAdapter(mContext, hostList)
            mRv.adapter = mAdapter
            showData()
        } else {
            mAdapter.setHostDataList(hostList)
        }
    }

    override fun initData() {
        disposable.add(viewModel.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    hostList = it
                    checkDns()
                })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBus(event: MessageEvent) {
        if (event.type == 2) {
            mAdapter.setHostDataList(hostList)
        }
    }

    private fun initView(view: View) {
        mRv = view.findViewById(R.id.rv)
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mTvEmpty = view.findViewById(R.id.tv_empty)
    }
}
