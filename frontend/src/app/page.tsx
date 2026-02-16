import Header from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import MarketOverview from "@/components/home/MarketOverview";
import CryptoTable from "@/components/home/CryptoTable";
import TrendingCoins from "@/components/home/TrendingCoins";
import LatestNews from "@/components/home/LatestNews";

export default function Home() {
  return (
    <div className="min-h-screen bg-bg-body">
      <Header />
      <div className="max-w-page mx-auto px-4 py-5">
        {/* Market Overview (FnGuide Snapshot 스타일) */}
        <MarketOverview />

        {/* Main content with sidebar */}
        <div className="flex gap-5 mt-5">
          {/* Main content area */}
          <div className="flex-1 min-w-0 space-y-5">
            <CryptoTable />
          </div>

          {/* Right sidebar */}
          <div className="hidden xl:flex flex-col gap-5 w-56 flex-shrink-0">
            <TrendingCoins />
            <LatestNews />
            <Sidebar />
          </div>
        </div>
      </div>
    </div>
  );
}
