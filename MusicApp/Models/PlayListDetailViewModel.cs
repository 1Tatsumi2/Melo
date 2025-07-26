using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace MusicApp.Models
{
	public class PlayListDetailViewModel
	{
		public int Ma_PlayList { get; set; }
		public string Ten_PlayList { get; set; }        // Tên playlist
		public string HinhAnh { get; set; }             // Hình ảnh của playlist
		public string AccountName { get; set; }         // Tên người dùng tạo playlist
		public int Ma_User { get; set; }
		public string Ten_User { get; set; }

		public int SoLuongBaiHat { get; set; } // Số lượng bài hát
		public System.TimeSpan ThoiLuongTongCong { get; set; } // Thời lượng bài hát đầu tiên

		public string SingersDisplay { get; set; }

		public List<SongViewModel> Songs { get; set; }
		public PlayListDetailViewModel()
		{
			Songs = new List<SongViewModel>();
		}
	}
}